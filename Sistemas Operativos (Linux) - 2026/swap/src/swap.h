#ifndef SWAP_H_
#define SWAP_H_

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <commons/log.h>
#include <commons/config.h>
#include "conexiones.h"
#include "sockets.h"

extern t_log* logger;
extern t_config* config;
extern FILE* archivo_swap;

int iniciar_swap(char* config_path);
void inicializar_archivo_swap(void);

#endif