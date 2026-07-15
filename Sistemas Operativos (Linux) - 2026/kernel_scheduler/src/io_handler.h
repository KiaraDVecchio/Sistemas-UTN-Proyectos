#ifndef IO_HANDLER_H_
#define IO_HANDLER_H_

#include <pthread.h>
#include "sockets.h"
#include "proceso.h"

// Registro de fds de cada tipo de IO conectado.
// fd == -1 → ese tipo de IO no está conectado aún.
typedef struct {
    int fd_sleep;
    int fd_stdin;
    int fd_stdout;
} t_io_fds;

extern t_io_fds         io_fds;
extern pthread_mutex_t  mutex_io_fds;

// Llamada desde manejar_cliente cuando llega HANDSHAKE_IO.
// Registra el fd y queda escuchando respuestas de esa IO.
void manejar_conexion_io(int fd);

// Envía una syscall de IO al módulo correspondiente y mueve
// el proceso a BLOCK. Llamada cuando la CPU hace SOLICITUD_IO.
void despachar_io(t_proceso* proceso, t_cod_op tipo_io, uint32_t param1, uint32_t param2);

// Si el proceso tiene un input de STDIN pendiente de escribir (porque se suspendio
// mientras lo esperaba), lo escribe ahora que sus segmentos ya son validos de nuevo.
// Llamada desde intentar_dessuspender justo despues de DESUSPENSION_OK.
void completar_stdin_pendiente(t_proceso* proceso);

#endif