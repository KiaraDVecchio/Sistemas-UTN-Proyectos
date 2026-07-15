#include "proceso.h"
#include "kernel_scheduler.h"
#include "compactacion_handler.h"
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include "planificador.h"

t_list* cola_ready = NULL;
t_list* cola_block = NULL;
t_list* cola_susp_ready = NULL;
t_list* procesos = NULL;
t_list* cpus_libres = NULL;
t_list** colas_ready_multinivel = NULL;
int cant_colas = 0;
char** algoritmo_por_cola = NULL;
bool queue_preemption = false;
uint32_t suspension_timeout = 0;

pthread_mutex_t mutex_estado = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t mutex_block = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t  cond_planificador = PTHREAD_COND_INITIALIZER;

char* algoritmo_de_planificacion = NULL;
uint32_t quantum = 0;

static uint32_t proximo_pid = 0;

static const char* NOMBRES_ESTADO[] = {"NEW", "READY", "EXEC", "BLOCK", "SUSP_BLOCK", "SUSP_READY", "EXIT"};

t_proceso* crear_proceso(char* path, int prioridad) {
    t_proceso* proceso = malloc(sizeof(t_proceso));
    proceso->estado = NEW;
    proceso->prioridad=prioridad;
    proceso->prioridad_original = prioridad;
    proceso->path_proceso = strdup(path);
    proceso->pc = 0;
    proceso->fd_cpu = -1;
    memset(&proceso->io_pendiente, 0, sizeof(proceso->io_pendiente));
    proceso->stdin_pendiente = NULL;
    proceso->stdin_pendiente_tam = 0;
    proceso->esperando_mem_alloc = false;
    proceso->id_seg_pendiente = 0;
    proceso->tamanio_pendiente = 0;

    pthread_mutex_lock(&mutex_estado);
    proceso->pid = proximo_pid++;
    list_add(procesos, proceso);
    pthread_mutex_unlock(&mutex_estado);

    log_info(logger, "## (%d) Se crea el proceso - Estado: NEW", proceso->pid);
    return proceso;
}

t_proceso* buscar_proceso_por_pid(uint32_t pid) {
    pthread_mutex_lock(&mutex_estado);
    t_proceso* encontrado = NULL;
    for (int i = 0; i < list_size(procesos); i++) {
        t_proceso* p = list_get(procesos, i);
        if (p->pid == pid) {
            encontrado = p;
            break;
        }
    }
    pthread_mutex_unlock(&mutex_estado);
    return encontrado;
}

static uint32_t _pid_buscado;
static bool _cmp_pid(void* elem) {
    return ((t_proceso*)elem)->pid == _pid_buscado;
}

const char* nombre_estado(t_estado estado){
    return NOMBRES_ESTADO[estado];
}

void cambiar_prioridad(t_proceso* proceso, int nueva_prioridad) {
    if (proceso->prioridad == nueva_prioridad) return;
    log_info(logger, "## %d Cambio de prioridad: %d - %d",
        proceso->pid, proceso->prioridad, nueva_prioridad);

    pthread_mutex_lock(&mutex_estado);

if (proceso->estado == READY && strcmp(algoritmo_de_planificacion, "CMN") == 0) {
        _pid_buscado = proceso->pid;
        list_remove_by_condition(colas_ready_multinivel[proceso->prioridad], _cmp_pid);
        proceso->prioridad = nueva_prioridad;
        list_add(colas_ready_multinivel[proceso->prioridad], proceso);
    } else {
        proceso->prioridad = nueva_prioridad;
    }

    //Actualizamos la prioridad que tiene registrada la CPU para q no desalojemos sin querer a un proceso q aumento su prioridad por herencia
    for (int i = 0; i < list_size(todas_las_cpus); i++) {
        t_cpu* cpu = list_get(todas_las_cpus, i);
        if (!cpu->libre && cpu->pid_actual == proceso->pid) {
            cpu->prioridad_proceso = nueva_prioridad;
            break;
        }
    }
    pthread_mutex_unlock(&mutex_estado);
}

void cambiar_estado(t_proceso* proceso, t_estado nuevo) {
    t_estado anterior = proceso->estado;
    log_info(logger, "## (%d) Pasa del estado %s al estado %s",
            proceso->pid,
            nombre_estado(anterior),
            nombre_estado(nuevo));

    // sacar de la lista anterior
    if (anterior == READY) {
        pthread_mutex_lock(&mutex_estado);
        _pid_buscado = proceso->pid;
        if(strcmp(algoritmo_de_planificacion,"CMN") == 0){
            list_remove_by_condition(colas_ready_multinivel[proceso->prioridad],_cmp_pid);
        }else{
            list_remove_by_condition(cola_ready,_cmp_pid);
        }
        pthread_mutex_unlock(&mutex_estado);
    } else if (anterior == BLOCK) {
        pthread_mutex_lock(&mutex_block);
        _pid_buscado = proceso->pid;
        list_remove_by_condition(cola_block,_cmp_pid);
        pthread_mutex_unlock(&mutex_block);
    } else if (anterior == SUSP_READY) {
        pthread_mutex_lock(&mutex_estado);
        _pid_buscado = proceso->pid;
        list_remove_by_condition(cola_susp_ready, _cmp_pid);
        pthread_mutex_unlock(&mutex_estado);
    }
    // SUSP_BLOCK no esta en ninguna lista, nada que sacar

    proceso->estado = nuevo;

    // agregar a la lista nueva
    if (nuevo == READY) {
        pthread_mutex_lock(&mutex_estado);
        if (strcmp(algoritmo_de_planificacion,"CMN")==0){
        list_add(colas_ready_multinivel[proceso->prioridad],proceso);
    }else{
        list_add(cola_ready, proceso);
    }
    pthread_cond_signal(&cond_planificador);
    pthread_mutex_unlock(&mutex_estado);
    verificar_desalojo_por_prioridad(proceso); //cada vez q llega un proceso a ready hay que verificar que no sea mas urgente que los demas 
    } else if (nuevo == BLOCK) {
        pthread_mutex_lock(&mutex_block);
        list_add(cola_block, proceso);
        pthread_mutex_unlock(&mutex_block);
        iniciar_timer_suspension(proceso);
    } else if (nuevo == SUSP_READY) {
        pthread_mutex_lock(&mutex_estado);
        list_add(cola_susp_ready, proceso);
        pthread_mutex_unlock(&mutex_estado);
    }
    // SUSP_BLOCK y EXIT no tienen lista propia
}

void agregar_primero_ready(t_proceso* proceso) {
    log_info(logger, "## (%d) Pasa del estado %s al estado %s",
            proceso->pid, nombre_estado(proceso->estado), nombre_estado(READY));
    pthread_mutex_lock(&mutex_estado);
    if (strcmp(algoritmo_de_planificacion, "CMN") == 0)
        list_add_in_index(colas_ready_multinivel[proceso->prioridad], 0, proceso);
    else
        list_add_in_index(cola_ready, 0, proceso);
    proceso->estado = READY;
    pthread_cond_signal(&cond_planificador);
    pthread_mutex_unlock(&mutex_estado);
}

typedef struct {
    uint32_t pid;
} t_timer_args;

static void* hilo_suspension_timeout(void* arg) {
    t_timer_args* a = arg;
    uint32_t pid = a->pid;
    free(a);

    struct timespec ts = {
        .tv_sec  = suspension_timeout / 1000,
        .tv_nsec = (long)(suspension_timeout % 1000) * 1000000L
    };
    nanosleep(&ts, NULL);

    t_proceso* proceso = buscar_proceso_por_pid(pid);
    if (proceso == NULL || proceso->estado != BLOCK) return NULL;

    // Un proceso bloqueado esperando un MEM_ALLOC pendiente no se autosuspende:
    // suspenderlo movería a SWAP sus segmentos YA existentes, y el reintento del
    // segmento nuevo quedaría desincronizado con esa suspensión. Se lo deja en BLOCK
    // hasta que intentar_reintentar_mem_alloc_pendientes() lo resuelva.
    if (proceso->esperando_mem_alloc) return NULL;

    log_info(logger, "## (%d) Superó SUSPENSION_TIMEOUT, pasando a SUSP_BLOCK", pid);
    cambiar_estado(proceso, SUSP_BLOCK);

    //pide a Kernel Memory que mueva los segmentos a SWAP
    pthread_mutex_lock(&mutex_fd_kernel_memory);
    int cod = SUSPENDER_PROCESO;
    send(fd_kernel_memory, &cod, sizeof(int), 0);
    send(fd_kernel_memory, &pid, sizeof(uint32_t), 0);
    recibir_operacion(fd_kernel_memory); // SUSPENSION_OK
    pthread_mutex_unlock(&mutex_fd_kernel_memory);

    //liberar memoria puede permitir dessuspender otro
    intentar_dessuspender();

    return NULL;
}

void iniciar_timer_suspension(t_proceso* proceso) {
    if (suspension_timeout == 0) return;
    t_timer_args* a = malloc(sizeof(t_timer_args));
    a->pid = proceso->pid;
    pthread_t hilo;
    pthread_create(&hilo, NULL, hilo_suspension_timeout, a);
    pthread_detach(hilo);
}
