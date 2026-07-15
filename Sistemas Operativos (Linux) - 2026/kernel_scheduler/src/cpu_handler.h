#ifndef CPU_HANDLER_H_
#define CPU_HANDLER_H_

#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <commons/log.h>
#include <commons/config.h>
#include "sockets.h"

extern t_log* logger;

void probar_conexion_con_cpu() ;

void enviar_proceso_a_cpu(int socket_dispatch, uint32_t pid);

t_desalojo recibir_desalojo_de_cpu(int socket_dispatch, uint32_t* pid);
void enviar_interrupcion(int fd_interrupt, uint32_t pid_interrumpir);
#endif /* CPU_HANDLER_H_ */