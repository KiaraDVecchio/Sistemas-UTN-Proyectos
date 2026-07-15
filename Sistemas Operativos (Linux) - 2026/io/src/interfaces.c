#include <io.h>
#include <pthread.h>

extern pthread_mutex_t mutex_socket;

extern t_log* logger;

void* hilo_instruccion_io(void* args_void) {
    t_hilo_io_args* args = (t_hilo_io_args*) args_void;

    switch (args->operacion) {
        case IO_SLEEP:
            ejecutar_sleep(args->pid, args->valor, args->socket_kernel);
            break;
        case IO_STDIN:
            ejecutar_stdin(args->pid, args->valor, args->socket_kernel);
            break;
        case IO_STDOUT:
            // Pasamos el buffer recibido del Kernel
            ejecutar_stdout(args->pid, args->valor, args->buffer, args->socket_kernel);
            break;
    }

    if(args->buffer != NULL) free(args->buffer); 
    free(args);
    return NULL;
}

void ejecutar_stdin(uint32_t pid, uint32_t tamaño, int socket_kernel) {
    log_info(logger, "## PID: %u - Inicio de IO", pid);
    log_info(logger, "## PID: %u - Ingrese %u caracteres:", pid, tamaño);
    
    // tamaño + 1 para el \0
    char* leido = malloc(tamaño + 1);

    if (fgets(leido, tamaño + 1, stdin) != NULL) {
        leido[strcspn(leido, "\n")] = 0;
        int real_len = strlen(leido);
        if(real_len < tamaño) {
            memset(leido + real_len, '\0', tamaño - real_len);
        }
    }

    // --- ESTRUCTURA DE ENVÍO AL KERNEL ---
    t_paquete* paquete = crear_paquete();
    paquete->codigo_operacion = IO_STDIN; // El Kernel debe saber que es una respuesta de STDIN
    
    agregar_a_paquete(paquete, &pid, sizeof(uint32_t));
    agregar_a_paquete(paquete, &tamaño, sizeof(uint32_t));
    agregar_a_paquete(paquete, leido, tamaño); // El buffer con lo que escribió el usuario

    pthread_mutex_lock(&mutex_socket);
    enviar_paquete(paquete, socket_kernel);
    pthread_mutex_unlock(&mutex_socket);

    eliminar_paquete(paquete);    
    log_info(logger, "## PID: %u - Fin de IO", pid);
    free(leido);
}

void ejecutar_sleep(uint32_t pid, uint32_t milisegundos, int socket_kernel) {
    log_info(logger, "## PID: %u - Inicio de IO", pid);
    log_info(logger, "## PID: %u - Haciendo sleep por %u milisegundos.", pid, milisegundos);
    
    usleep(milisegundos * 1000);

    t_paquete* paquete = crear_paquete();
    paquete->codigo_operacion = IO_SLEEP; 
    agregar_a_paquete(paquete, &pid, sizeof(uint32_t));
    

    pthread_mutex_lock(&mutex_socket);
    enviar_paquete(paquete, socket_kernel);
    pthread_mutex_unlock(&mutex_socket);

    eliminar_paquete(paquete);

    log_info(logger, "## PID: %u - Fin de IO", pid);
}

void ejecutar_stdout(uint32_t pid, uint32_t tamaño, void* contenido, int socket_kernel) {
    log_info(logger, "## PID: %u - Inicio de IO", pid);

    log_info(logger, "## PID: %u - %.*s", pid, (int)tamaño, (char*)contenido);
    printf("Contenido STDOUT (PID %u): %.*s\n", pid, (int)tamaño, (char*)contenido);

    t_paquete* paquete = crear_paquete();
    paquete->codigo_operacion = IO_STDOUT; // DEFINIR EL CODIGO
    agregar_a_paquete(paquete, &pid, sizeof(uint32_t));

    pthread_mutex_lock(&mutex_socket);
    enviar_paquete(paquete, socket_kernel);
    pthread_mutex_unlock(&mutex_socket);

    eliminar_paquete(paquete);

    log_info(logger, "## PID: %u - Fin de IO", pid);
}