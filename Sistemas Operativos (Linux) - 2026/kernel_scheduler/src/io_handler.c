#include "io_handler.h"
#include "kernel_scheduler.h"
#include "compactacion_handler.h"
#include <commons/log.h>

t_io_fds        io_fds       = { .fd_sleep = -1, .fd_stdin = -1, .fd_stdout = -1 };
pthread_mutex_t mutex_io_fds = PTHREAD_MUTEX_INITIALIZER;

static const char* nombre_io(t_cod_op tipo) {
    switch (tipo) {
        case IO_SLEEP:  return "SLEEP";
        case IO_STDIN:  return "STDIN";
        case IO_STDOUT: return "STDOUT";
        default:        return "DESCONOCIDO";
    }
}

static int fd_de_tipo(t_cod_op tipo) {
    switch (tipo) {
        case IO_SLEEP:  return io_fds.fd_sleep;
        case IO_STDIN:  return io_fds.fd_stdin;
        case IO_STDOUT: return io_fds.fd_stdout;
        default:        return -1;
    }
}


static void resolver_fin_io(t_proceso* proceso) {
    if (proceso->estado == SUSP_BLOCK) {
        cambiar_estado(proceso, SUSP_READY);
        intentar_dessuspender();
    } else {
        cambiar_estado(proceso, READY);
    }
}

static void fin_io_sleep(t_list* datos) {
    uint32_t pid = *(uint32_t*)list_get(datos, 0);
    log_info(logger, "## (%d) finalizó IO y pasa a READY / SUSP. READY", pid);
    t_proceso* proceso = buscar_proceso_por_pid(pid);
    if (proceso == NULL) { log_error(logger, "SLEEP: PID %d no encontrado", pid); return; }
    resolver_fin_io(proceso);
}


static void escribir_stdin_en_memoria(uint32_t pid, uint32_t dir_logica, uint32_t tamanio, void* buffer) {
    pthread_mutex_lock(&mutex_fd_kernel_memory);
    t_paquete* paquete = crear_paquete();
    paquete->codigo_operacion = ESCRITURA_MEMORIA;
    agregar_a_paquete(paquete, &pid,        sizeof(uint32_t));
    agregar_a_paquete(paquete, &dir_logica, sizeof(uint32_t));
    agregar_a_paquete(paquete, &tamanio,    sizeof(uint32_t));
    agregar_a_paquete(paquete, buffer,      tamanio);
    enviar_paquete(paquete, fd_kernel_memory);
    eliminar_paquete(paquete);
    int respuesta = recibir_operacion(fd_kernel_memory);
    pthread_mutex_unlock(&mutex_fd_kernel_memory);
    if (respuesta != RESPUESTA_OK)
        log_error(logger, "STDIN: Kernel Memory no confirmó escritura para PID %d", pid);
}

void completar_stdin_pendiente(t_proceso* proceso) {
    if (proceso->stdin_pendiente == NULL) return;
    escribir_stdin_en_memoria(proceso->pid, proceso->io_pendiente.dir_logica,
                              proceso->stdin_pendiente_tam, proceso->stdin_pendiente);
    free(proceso->stdin_pendiente);
    proceso->stdin_pendiente = NULL;
}

static void fin_io_stdin(t_list* datos) {
    uint32_t pid     = *(uint32_t*)list_get(datos, 0);
    uint32_t tamanio = *(uint32_t*)list_get(datos, 1);
    void*    buffer  =             list_get(datos, 2);

    t_proceso* proceso = buscar_proceso_por_pid(pid);
    if (proceso == NULL) { log_error(logger, "STDIN: PID %d no encontrado", pid); return; }

    if (proceso->estado == SUSP_BLOCK) {
        proceso->stdin_pendiente = malloc(tamanio);
        memcpy(proceso->stdin_pendiente, buffer, tamanio);
        proceso->stdin_pendiente_tam = tamanio;
    } else {
        escribir_stdin_en_memoria(pid, proceso->io_pendiente.dir_logica, tamanio, buffer);
    }

    log_info(logger, "## (%d) finalizó IO y pasa a READY / SUSP. READY", pid);
    resolver_fin_io(proceso);
}

static void fin_io_stdout(t_list* datos) {
    uint32_t pid = *(uint32_t*)list_get(datos, 0);

    t_proceso* proceso = buscar_proceso_por_pid(pid);
    if (proceso == NULL) { log_error(logger, "STDOUT: PID %d no encontrado", pid); return; }
    log_info(logger, "## (%d) finalizó IO y pasa a READY / SUSP. READY", pid);
    resolver_fin_io(proceso);
}


static void manejar_respuestas_io(int fd, t_cod_op tipo_io) {
    while (1) {
        int cod_op = recibir_operacion(fd);
        if (cod_op == -1) {
            log_warning(logger, "IO %s desconectada.", nombre_io(tipo_io));
            pthread_mutex_lock(&mutex_io_fds);
            if (tipo_io == IO_SLEEP)  io_fds.fd_sleep  = -1;
            if (tipo_io == IO_STDIN)  io_fds.fd_stdin  = -1;
            if (tipo_io == IO_STDOUT) io_fds.fd_stdout = -1;
            pthread_mutex_unlock(&mutex_io_fds);
            break;
        }

        t_list* datos = recibir_paquete(fd);

        switch (cod_op) {
            case IO_SLEEP:  fin_io_sleep(datos);  break;
            case IO_STDIN:  fin_io_stdin(datos);  break;
            case IO_STDOUT: fin_io_stdout(datos); break;
            default:
                log_warning(logger, "Respuesta desconocida de IO: %d", cod_op);
                break;
        }

        list_destroy_and_destroy_elements(datos, free);
    }
}

void manejar_conexion_io(int fd) {
    t_cod_op tipo_io;
    if (recv(fd, &tipo_io, sizeof(t_cod_op), MSG_WAITALL) <= 0) {
        log_error(logger, "Error leyendo tipo de IO.");
        return;
    }

    pthread_mutex_lock(&mutex_io_fds);
    switch (tipo_io) {
        case IO_SLEEP:
            io_fds.fd_sleep = fd;
            log_info(logger, "## IO Conectada: SLEEP (fd=%d)", fd);
            break;
        case IO_STDIN:
            io_fds.fd_stdin = fd;
            log_info(logger, "## IO Conectada: STDIN (fd=%d)", fd);
            break;
        case IO_STDOUT:
            io_fds.fd_stdout = fd;
            log_info(logger, "## IO Conectada: STDOUT (fd=%d)", fd);
            break;
        default:
            log_warning(logger, "IO con tipo desconocido: %d", tipo_io);
            pthread_mutex_unlock(&mutex_io_fds);
            return;
    }
    pthread_mutex_unlock(&mutex_io_fds);

    manejar_respuestas_io(fd, tipo_io);
}


void despachar_io(t_proceso* proceso, t_cod_op tipo_io, uint32_t param1, uint32_t param2) {

    log_info(logger, "## (%d) - Solicitó syscall: %s", proceso->pid, nombre_io(tipo_io));

    proceso->io_pendiente.dir_logica = param1;
    proceso->io_pendiente.tamanio    = param2;

    cambiar_estado(proceso, BLOCK);

    pthread_mutex_lock(&mutex_io_fds);
    int fd_io = fd_de_tipo(tipo_io);
    pthread_mutex_unlock(&mutex_io_fds);

    if (fd_io == -1) {
        log_error(logger, "(%d) Solicita %s pero ese módulo IO no está conectado.",
                  proceso->pid, nombre_io(tipo_io));
        return;
    }

    if (tipo_io == IO_STDOUT) {
        uint32_t dir_logica = param1;
        uint32_t tamanio    = param2;

        // Pedirle a Kernel Memory los bytes a imprimir
        pthread_mutex_lock(&mutex_fd_kernel_memory);
        t_paquete* req = crear_paquete();
        req->codigo_operacion = LECTURA_MEMORIA;
        agregar_a_paquete(req, &proceso->pid, sizeof(uint32_t));
        agregar_a_paquete(req, &dir_logica,   sizeof(uint32_t));
        agregar_a_paquete(req, &tamanio,      sizeof(uint32_t));
        enviar_paquete(req, fd_kernel_memory);
        eliminar_paquete(req);

        int cod = recibir_operacion(fd_kernel_memory);
        t_list* mem_datos = recibir_paquete(fd_kernel_memory);
        pthread_mutex_unlock(&mutex_fd_kernel_memory);
        void* contenido = list_get(mem_datos, 0);

        // Mandar a STDOUT: {pid, tamaño, contenido}
        t_paquete* paquete = crear_paquete();
        paquete->codigo_operacion = IO_STDOUT;
        agregar_a_paquete(paquete, &proceso->pid, sizeof(uint32_t));
        agregar_a_paquete(paquete, &tamanio,      sizeof(uint32_t));
        agregar_a_paquete(paquete, contenido,     tamanio);
        enviar_paquete(paquete, fd_io);
        eliminar_paquete(paquete);
        list_destroy_and_destroy_elements(mem_datos, free);

    } else {

        t_paquete* paquete = crear_paquete();
        paquete->codigo_operacion = tipo_io;
        agregar_a_paquete(paquete, &proceso->pid, sizeof(uint32_t));
        agregar_a_paquete(paquete, &param1,       sizeof(uint32_t));
        if (tipo_io == IO_STDIN) {
            agregar_a_paquete(paquete, &param2, sizeof(uint32_t));
        }
        enviar_paquete(paquete, fd_io);
        eliminar_paquete(paquete);
    }

    pthread_cond_signal(&cond_planificador);
}