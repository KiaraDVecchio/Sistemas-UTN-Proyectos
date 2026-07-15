#ifndef UTILS_CONEXIONES_H_
#define UTILS_CONEXIONES_H_

#include <commons/log.h>
#include "sockets.h"

// Función general para conectarse a un servidor y enviar handshake
int conectarse_a_modulo(char* ip, char* puerto, t_log* logger, t_cod_op handshake);

#endif