#include "mutex_handler.h"
#include "planificador.h"
#include "compactacion_handler.h"
#include <string.h>
#include <commons/log.h>

extern t_log* logger;

static t_list* mutexes = NULL;
static pthread_mutex_t mutex_lista = PTHREAD_MUTEX_INITIALIZER;

void mutex_inicializar(void) {
    mutexes = list_create();
}

static t_mutex* buscar_mutex(char* nombre) {
    for (int i = 0; i < list_size(mutexes); i++) {
        t_mutex* mutex = list_get(mutexes, i);
        if (strcmp(mutex->nombre, nombre) == 0) return mutex;
    }
    return NULL;
}

// busca la prioridad mas alta entre los procesos que esperan algun
// mutex que este pid tiene tomado. -1 si no le esta esperando nadie
static int prioridad_heredada(uint32_t pid) {
    int mejor = -1;

    pthread_mutex_lock(&mutex_lista);
    for (int i = 0; i < list_size(mutexes); i++) {
        t_mutex* mutex = list_get(mutexes, i);
        if (mutex->pid_tomador != pid) continue;

        for (int j = 0; j < list_size(mutex->cola_espera); j++) {
            t_proceso* esperando = list_get(mutex->cola_espera, j);
            if (mejor == -1 || esperando->prioridad < mejor) mejor = esperando->prioridad;
        }
    }
    pthread_mutex_unlock(&mutex_lista);

    return mejor;
}

// recalcula la prioridad de un proceso segun lo que le toque heredar
// de los que esperan los mutex que tiene tomados, o su prioridad
// original si nadie lo esta esperando
static void recalcular_prioridad(t_proceso* proceso) {
    int heredada = prioridad_heredada(proceso->pid);
    int nueva = proceso->prioridad_original;
    if (heredada != -1 && heredada < nueva) nueva = heredada;
    cambiar_prioridad(proceso, nueva);
}

// pasa el mutex al siguiente de la cola de espera si hay alguno,
// o lo deja libre. el siguiente recalcula su prioridad antes de pasar a READY
// porque puede heredar de los que sigan esperando
static void transferir_mutex(t_mutex* mutex) {
    pthread_mutex_lock(&mutex_lista);
    t_proceso* siguiente = list_is_empty(mutex->cola_espera) ? NULL : list_remove(mutex->cola_espera, 0);
    mutex->pid_tomador = (siguiente != NULL) ? siguiente->pid : (uint32_t)-1;
    pthread_mutex_unlock(&mutex_lista);

    if (siguiente != NULL) {
        log_info(logger, "## (%d) Toma el Mutex %s", siguiente->pid, mutex->nombre);
        recalcular_prioridad(siguiente);
        if (siguiente->estado == SUSP_BLOCK) {
            cambiar_estado(siguiente, SUSP_READY);
            intentar_dessuspender();
        } else {
            cambiar_estado(siguiente, READY);
        }
    }
}

void mutex_create(t_proceso* proceso, char* nombre, t_cpu* cpu) {
    log_info(logger, "## (%d) - Solicitó syscall: MUTEX_CREATE", proceso->pid);
    pthread_mutex_lock(&mutex_lista);
    t_mutex* mutex = buscar_mutex(nombre);
    if (mutex == NULL){ 
        mutex = malloc(sizeof(t_mutex));
        strncpy(mutex->nombre, nombre, 49);
        mutex->nombre[49] = '\0';
        mutex->pid_tomador = (uint32_t)-1;
        mutex->cola_espera = list_create(); //se crea con la cola de espera vacia
        list_add(mutexes, mutex);
    }
    pthread_mutex_unlock(&mutex_lista);
    cambiar_estado(proceso, READY);
}

void mutex_lock(t_proceso* proceso, char* nombre, t_cpu* cpu) {
    log_info(logger, "## (%d) - Solicitó syscall: MUTEX_LOCK", proceso->pid);
    pthread_mutex_lock(&mutex_lista);
    t_mutex* mutex = buscar_mutex(nombre);

    if (mutex == NULL) {
        pthread_mutex_unlock(&mutex_lista);
        log_error(logger, "## (%d) intenta lockear mutex '%s' que no existe", proceso->pid, nombre);
        cambiar_estado(proceso, READY);
        return;
    }

    //check+set atomico bajo mutex_lista evita que dos CPUs tomen el mismo mutex libre
    bool ya_tomado = (mutex->pid_tomador != (uint32_t)-1);
    if (!ya_tomado) {
        mutex->pid_tomador = proceso->pid; //se lo damos al proceso
    }
    pthread_mutex_unlock(&mutex_lista);

    if (!ya_tomado) {
        log_info(logger, "## (%d) Toma el Mutex %s", proceso->pid, nombre);
        cambiar_estado(proceso, READY);
    } else { //si ya estaba tomado bloqueamos el proceso
        cambiar_estado(proceso, BLOCK);
        pthread_mutex_lock(&mutex_lista);
        list_add(mutex->cola_espera, proceso); //y lo metemos en la cola
        //cuando el proceso q lo tiene haga unlock, este proceso vuelve a ready
        uint32_t pid_tomador = mutex->pid_tomador; //copiamos antes de soltar el lock para no leer un valor viejo
        pthread_mutex_unlock(&mutex_lista);
        //si somos mas prioritarios que el que tiene el mutex, se lo prestamos
        t_proceso* tomador = buscar_proceso_por_pid(pid_tomador);
        if (tomador != NULL) recalcular_prioridad(tomador);
    }
}

void mutex_unlock(t_proceso* proceso, char* nombre, t_cpu* cpu) {
    log_info(logger, "## (%d) - Solicitó syscall: MUTEX_UNLOCK", proceso->pid);
    pthread_mutex_lock(&mutex_lista);
    t_mutex* mutex = buscar_mutex(nombre);
    pthread_mutex_unlock(&mutex_lista);

    if (mutex == NULL || mutex->pid_tomador != proceso->pid) { //verificamos que sea el dueño
        log_error(logger, "## (%d) intenta unlockear mutex '%s' que no tiene", proceso->pid, nombre);
        cambiar_estado(proceso, READY);
        return;
    }

    log_info(logger, "## (%d) Libera el Mutex %s", proceso->pid, nombre);

    transferir_mutex(mutex);

    //quien libero puede seguir con prioridad heredada de otro mutex que tenga tomado
    recalcular_prioridad(proceso);
    cambiar_estado(proceso, READY);
}

// cuando un proceso termina, los mutex que tenia tomados pasan
// al siguiente en cada cola de espera (si hay alguno)
void mutex_liberar_de_proceso(t_proceso* proceso) {
    pthread_mutex_lock(&mutex_lista);
    t_list* propios = list_create();
    for (int i = 0; i < list_size(mutexes); i++) {
        t_mutex* mutex = list_get(mutexes, i);
        if (mutex->pid_tomador == proceso->pid) list_add(propios, mutex);
    }
    pthread_mutex_unlock(&mutex_lista);

    for (int i = 0; i < list_size(propios); i++) {
        t_mutex* mutex = list_get(propios, i);
        log_info(logger, "## (%d) Libera el Mutex %s", proceso->pid, mutex->nombre);
        transferir_mutex(mutex);
    }

    list_destroy(propios);
}
