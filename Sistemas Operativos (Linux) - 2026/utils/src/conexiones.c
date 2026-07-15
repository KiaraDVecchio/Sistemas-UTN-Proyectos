#include <conexiones.h>

int conectarse_a_modulo(char* ip, char* puerto, t_log* logger, t_cod_op handshake) {
    int fd = crear_conexion(ip, puerto);
    if (fd != -1) {
        send(fd, &handshake, sizeof(t_cod_op), 0);
    } else {
        log_error(logger, "No se pudo establecer conexión con el puerto %s", puerto);
    }
    return fd;
}