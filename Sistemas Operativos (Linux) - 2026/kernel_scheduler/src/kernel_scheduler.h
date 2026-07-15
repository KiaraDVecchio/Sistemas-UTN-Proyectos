#ifndef KERNEL_SCHEDULER_H_
#define KERNEL_SCHEDULER_H_

#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <pthread.h>
#include <commons/log.h>
#include <commons/config.h>
#include "sockets.h"
#include "conexiones.h"

extern t_log* logger;
extern int fd_kernel_memory;
extern t_config* config;

extern pthread_mutex_t mutex_fd_kernel_memory;
extern volatile bool compactando;

int iniciar_kernel(char* config_path, char* path_proceso_inicial);

#endif