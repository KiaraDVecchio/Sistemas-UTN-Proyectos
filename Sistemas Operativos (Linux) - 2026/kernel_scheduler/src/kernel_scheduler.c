#include <pthread.h>
#include <commons/log.h>
#include <commons/config.h>
#include "conexiones.h"
#include "kernel_scheduler.h"
#include "proceso.h"
#include "io_handler.h"
#include "planificador.h"
#include "mutex_handler.h"
#include "compactacion_handler.h"


t_log* logger;
t_config* config;
int fd_kernel_memory = -1;
pthread_mutex_t mutex_fd_kernel_memory = PTHREAD_MUTEX_INITIALIZER;
volatile bool   compactando = false;


// manejar_cliente
// Hilo que atiende a un cliente recién conectado.
// El primer mensaje determina si es CPU o IO,
// y delega a la función correspondiente.
void manejar_cliente(void* arg) {
    int fd = *(int*)arg;
    free(arg);

    int cod_op = recibir_operacion(fd);
    if (cod_op == -1) return;

    switch (cod_op) {
        case HANDSHAKE_CPU:
            registrar_cpu_dispatch(fd);
            break;

        case HANDSHAKE_CPU_INTERRUPT:
            registrar_cpu_interrupt(fd);
            break;


        case HANDSHAKE_IO:
            manejar_conexion_io(fd);
            break;

        default:
            log_warning(logger, "Operación desconocida en handshake: %d", cod_op);
            break;
    }
}

static char** parsear_algoritmos(const char*str, int*cantidad){ //ej:[FIFO,RR,RR]
    char* sin_corchetes = strdup(str + 1); //saco el primero
    sin_corchetes[strlen(sin_corchetes) - 1] = '\0'; //saco el ultimo

    //cant colas:una mas que las comas
    int n = 1;
    for(char* p = sin_corchetes; *p; p++){
        if (*p ==',') n++;
    }

    char** algoritmos = malloc(n * sizeof(char*));
    int i = 0;
    char* token = strtok(sin_corchetes,",");
    while(token != NULL){
        algoritmos[i++] = strdup(token);
        token = strtok(NULL,",");
    }

    *cantidad = n;
    free(sin_corchetes);
    return algoritmos;
}

int iniciar_kernel(char* config_path, char* path_proceso_inicial) {
    config = config_create(config_path);
    if (config == NULL) {
        printf("No se pudo crear el config: %s\n", config_path);
        return -1;
    }

    char* log_level_str = config_get_string_value(config, "LOG_LEVEL");
    logger = log_create("kernel.log", "KERNEL", true, log_level_from_string(log_level_str));


    algoritmo_de_planificacion = config_get_string_value(config, "PLANIFICATION_ALGORITHM");
    quantum = (uint32_t)config_get_int_value(config, "RR_QUANTUM");
    if(strcmp(algoritmo_de_planificacion,"CMN")==0){
        char* queues_string = config_get_string_value(config, "QUEUES_ALGORITHMS");
        algoritmo_por_cola = parsear_algoritmos(queues_string, &cant_colas);

        colas_ready_multinivel = malloc(cant_colas * sizeof(t_list*));
        for(int i=0; i<cant_colas;i++) {
        colas_ready_multinivel[i] =list_create();
    }

    char* preemption_string = config_get_string_value(config,"QUEUE_PREEMPTION");//leemos si el desalojo esta activado
    queue_preemption = (strcmp(preemption_string,"TRUE")==0);
    }


    cola_ready = list_create();
    cola_block = list_create();
    cola_susp_ready = list_create();
    procesos = list_create();
    cpus_libres = list_create();
    suspension_timeout = (uint32_t)config_get_int_value(config, "SUSPENSION_TIMEOUT");

    //Conectarse a Kernel Memory (dependencia obligatoria antes de levantar servidor)
    char* ip_m = config_get_string_value(config, "IP_MEMORY");
    char* puerto_m = config_get_string_value(config, "PUERTO_MEMORY");
    fd_kernel_memory = conectarse_a_modulo(ip_m, puerto_m, logger, HANDSHAKE_KERNEL);
    if (fd_kernel_memory != -1)
        log_info(logger, "## Conectado a Kernel Memory");

    //b)creo pid 0 y arranco el planificador
    t_proceso* proceso_inicial = crear_proceso(path_proceso_inicial, 0);
    
        //Avisamos a mem que existe el proceso, porque despues la cpu le pide el contexto
    t_paquete* paq = crear_paquete();
    paq->codigo_operacion = CREAR_PROCESO;
    agregar_a_paquete(paq,&proceso_inicial->pid, sizeof(uint32_t));
    uint32_t path_len = (uint32_t)strlen(path_proceso_inicial) + 1;
    agregar_a_paquete(paq, path_proceso_inicial, path_len); //pasamos el path del archivo de instrucc
    enviar_paquete(paq,fd_kernel_memory);
    eliminar_paquete(paq);
    recibir_operacion(fd_kernel_memory); //tengo q leer el respuesta_ok que manda kernel memory cuando crea el proceso 
    
    iniciar_planificador();
    cambiar_estado(proceso_inicial,READY);
    mutex_inicializar();
    iniciar_escucha_kernel_memory();

    //Levantar servidor para CPU e IO
    char* puerto = config_get_string_value(config, "PUERTO_ESCUCHA");
    int server_fd = iniciar_servidor(puerto);
    log_info(logger, "Servidor Kernel Scheduler escuchando en puerto %s", puerto);

    //Aceptar conexiones indefinidamente
    while (1) {
        int cliente_fd = esperar_cliente(server_fd);
        pthread_t hilo;
        int* p_fd = malloc(sizeof(int));
        *p_fd = cliente_fd;
        pthread_create(&hilo, NULL, (void*)manejar_cliente, p_fd);
        pthread_detach(hilo);
    }
    return 0;
}