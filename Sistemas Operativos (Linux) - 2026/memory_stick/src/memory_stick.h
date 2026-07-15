#ifndef MEMORY_STICK_H_
#define MEMORY_STICK_H_

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <commons/log.h>
#include <commons/config.h>
#include <unistd.h>
#include "conexiones.h"
#include "sockets.h"

extern t_log* logger;
extern t_config* config;
extern void* espacio_usuario; 

void iniciar_memory_stick(char* config_path, char* tamanio);


void finalizar_memory_stick();

#endif