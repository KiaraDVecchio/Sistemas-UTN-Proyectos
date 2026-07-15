#include "io.h"
#include <string.h>
#include <pthread.h>

t_log* logger;
t_config* config;
pthread_mutex_t mutex_socket;

// Convierte el string del tipo ("SLEEP", "STDIN", "STDOUT")
// al código de operación correspondiente para mandarlo al Kernel.
static t_cod_op tipo_a_cod_op(char* tipo) {
    if (strcmp(tipo, "SLEEP")  == 0) return IO_SLEEP;
    if (strcmp(tipo, "STDIN")  == 0) return IO_STDIN;
    if (strcmp(tipo, "STDOUT") == 0) return IO_STDOUT;
    return -1;
}

int iniciar_io(char* config_path, char* tipo) {
    config = config_create(config_path);
    char* log_level_str = config_get_string_value(config, "LOG_LEVEL");
    logger = log_create("io.log", "IO", true, log_level_from_string(log_level_str));

    char* ip     = config_get_string_value(config, "IP_KERNEL");
    char* puerto = config_get_string_value(config, "PUERTO_KERNEL");
    pthread_mutex_init(&mutex_socket, NULL);

    // 1. Conectarse y mandar HANDSHAKE_IO
    int fd_kernel = conectarse_a_modulo(ip, puerto, logger, HANDSHAKE_IO);
    if (fd_kernel == -1) {
        log_error(logger, "No se pudo conectar al Kernel Scheduler");
        return -1;
    }
    log_info(logger, "## Conectado a Kernel Scheduler");

    // 2. Mandar el tipo de IO para que el Kernel sepa quién somos
    t_cod_op mi_tipo = tipo_a_cod_op(tipo);
    send(fd_kernel, &mi_tipo, sizeof(t_cod_op), 0);

    // 3. Quedarse escuchando pedidos del Kernel
    while (1) {
    int cod_op = recibir_operacion(fd_kernel);
    if (cod_op == -1) {
        log_warning(logger, "Kernel desconectado. Cerrando IO.");
        break;
    }

    // --- DESEMPAQUETAR PID Y VALOR ---
    t_list* paquete = recibir_paquete(fd_kernel);
    // Para SLEEP y STDOUT: [pid, valor]. Para STDIN: [pid, dir_logica, tamanio]
    uint32_t* pid_ptr = list_get(paquete, 0);
    uint32_t* valor_ptr = list_get(paquete, 1);

    // Creamos los argumentos para ejecutar la instruccion
    t_hilo_io_args* args = malloc(sizeof(t_hilo_io_args));
    args->pid = *pid_ptr;
    args->valor = *valor_ptr;
    args->socket_kernel = fd_kernel;
    args->operacion = cod_op;
    args->buffer = NULL; // DEFAULT

    if (cod_op == IO_STDIN) {
        // el tamanio esta en indice 2, no en 1 (indice 1 es dir_logica)
        uint32_t* tamanio_ptr = list_get(paquete, 2);
        args->valor = *tamanio_ptr;
    } else if (cod_op == IO_STDOUT) {
        char* contenido = list_get(paquete, 2);
        args->buffer = malloc(*valor_ptr);
        memcpy(args->buffer, contenido, *valor_ptr);
    }

    list_destroy_and_destroy_elements(paquete, free);
    // IO es de un solo hilo: procesamos sincrónicamente un pedido a la vez
    hilo_instruccion_io(args);
}
    pthread_mutex_destroy(&mutex_socket);
    return 0;
}

