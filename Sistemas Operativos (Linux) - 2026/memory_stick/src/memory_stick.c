#include "memory_stick.h"
#include <pthread.h>

t_log* logger;
t_config* config;
void* espacio_usuario;
int server_fd;
int memory_delay;
// una CPU por conexion/hilo: sin este mutex dos hilos podrian pisarse a mitad
// de un memcpy sobre el mismo espacio_usuario (con mas de una CPU conectada)
pthread_mutex_t mutex_espacio_usuario = PTHREAD_MUTEX_INITIALIZER;

void atender_operaciones_memoria(int fd) {
    while (1) {
        int cod_op = recibir_operacion(fd);

        if (cod_op <= 0) {
            log_warning(logger, "Se cerró la conexión en el FD %d.", fd);
            break;
        }

        switch (cod_op) {
            case ESCRITURA_MEMORIA: {
                t_list* peticion = recibir_paquete(fd);
                uint32_t dir = *(uint32_t*)list_get(peticion, 0);
                uint32_t size = *(uint32_t*)list_get(peticion, 1);
                void* datos_a_escribir = list_get(peticion, 2);

                //Dealay es MS - Usleep es en microsegundos por eso * 1000
                usleep(memory_delay * 1000);

                log_info(logger, "## Escritura de %u bytes", size);
                pthread_mutex_lock(&mutex_espacio_usuario);
                memcpy((char*)espacio_usuario + dir, datos_a_escribir, size);
                pthread_mutex_unlock(&mutex_espacio_usuario);

                int respuesta = RESPUESTA_OK;
                send(fd, &respuesta, sizeof(int), 0);

                list_destroy_and_destroy_elements(peticion, free);
                break;
            }

            case LECTURA_MEMORIA: {
                t_list* peticion = recibir_paquete(fd);
                uint32_t dir = *(uint32_t*)list_get(peticion, 0);
                uint32_t size = *(uint32_t*)list_get(peticion, 1);

                usleep(memory_delay * 1000);

                log_info(logger, "## Lectura de %u bytes", size);
                void* datos_leidos = malloc(size);
                pthread_mutex_lock(&mutex_espacio_usuario);
                memcpy(datos_leidos,(char*)espacio_usuario + dir,size);
                pthread_mutex_unlock(&mutex_espacio_usuario);
                t_paquete* paquete_respuesta = crear_paquete();
                paquete_respuesta->codigo_operacion = DATOS_LECTURA;
                agregar_a_paquete(paquete_respuesta, datos_leidos, size);
                enviar_paquete(paquete_respuesta, fd);
                eliminar_paquete(paquete_respuesta);
                log_info(logger,"Leyo %u",datos_leidos);
                list_destroy_and_destroy_elements(peticion, free);
                break;
            }

            default:
                log_warning(logger, "Operación desconocida: %d", cod_op);
                break;
        }
    }
    close(fd);
}

void manejar_cpu(void* arg) {
    int fd_cpu = *(int*)arg;
    free(arg);

    // Recibir Handshake
    uint32_t id_cpu;
    t_cod_op hs;
    recv(fd_cpu, &hs, sizeof(t_cod_op), MSG_WAITALL);
    if(hs!=HANDSHAKE_CPU){
        log_error(logger, "Lo que llego no es una cpu");
        return;
    }

    recv(fd_cpu, &id_cpu, sizeof(uint32_t), MSG_WAITALL);
    log_info(logger, "## CPU %u Conectada", id_cpu);

    atender_operaciones_memoria(fd_cpu);
}

void manejar_kernel_memory(void* arg) {
    int fd_kmem = *(int*)arg;
    free(arg);

    log_info(logger, "## Escuchando pedidos de Kernel Memory (STDIN/STDOUT) en la conexión de handshake");
    atender_operaciones_memoria(fd_kmem);
}

void iniciar_memory_stick(char* config_path, char* tamano_str) {
    config = config_create(config_path);
    logger = log_create("memory_stick.log", "MS", true, LOG_LEVEL_INFO);

    memory_delay = config_get_int_value(config, "MEMORY_DELAY");
    
    uint32_t tamano = (uint32_t)atoi(tamano_str);
    espacio_usuario = malloc(tamano);
    if (espacio_usuario == NULL) {
        log_error(logger, "No se pudo reservar la memoria física");
        exit(EXIT_FAILURE);
    }
    memset(espacio_usuario, 0, tamano);

    
    char* ip_kmem = config_get_string_value(config, "IP_MEMORY");
    char* puerto_kmem = config_get_string_value(config, "PUERTO_MEMORY");
    char* puerto_escucha = config_get_string_value(config, "PUERTO_ESCUCHA");
    int fd_kmem = crear_conexion(ip_kmem, puerto_kmem);

   int op = HANDSHAKE_MEMORY_STICK; 
    send(fd_kmem, &op, sizeof(int), 0);
    send(fd_kmem, &tamano, sizeof(uint32_t), 0);
    uint32_t len_puerto = strlen(puerto_escucha) + 1;
    send(fd_kmem, &len_puerto, sizeof(uint32_t), 0);
    send(fd_kmem, puerto_escucha, len_puerto, 0);

    log_info(logger, "## Conectado a Kernel Memory");

    int* fd_kmem_ptr = malloc(sizeof(int));
    *fd_kmem_ptr = fd_kmem;
    pthread_t hilo_kmem;
    pthread_create(&hilo_kmem, NULL, (void*)manejar_kernel_memory, fd_kmem_ptr);
    pthread_detach(hilo_kmem);

    server_fd = iniciar_servidor(puerto_escucha);
    
    log_info(logger, "Memory Stick listo para recibir CPUs en puerto %s", puerto_escucha);

 
    while (1) {
        int* fd_cpu = malloc(sizeof(int));
        *fd_cpu = esperar_cliente(server_fd);
        
        pthread_t hilo;
        pthread_create(&hilo, NULL, (void*)manejar_cpu, fd_cpu);
        pthread_detach(hilo);
    }
}

void finalizar_memory_stick() {
    free(espacio_usuario);
    config_destroy(config);
    close(server_fd);
    log_destroy(logger);
}