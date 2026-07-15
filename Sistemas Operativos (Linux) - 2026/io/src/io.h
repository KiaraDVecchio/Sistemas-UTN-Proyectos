#ifndef IO_H_
#define IO_H_

#include <stdio.h>
#include <stdlib.h>
#include <commons/log.h>
#include <commons/config.h>
#include "conexiones.h"
#include "sockets.h"

extern t_log* logger;
extern t_config* config;
extern pthread_mutex_t mutex_socket;

typedef struct {
    uint32_t pid;
    uint32_t valor; // ms SLEEP o Tamaño STDIN-STDOUT
    int socket_kernel;
    t_cod_op operacion;
    void* buffer;
} t_hilo_io_args;

// Función principal de inicialización del módulo
int iniciar_io(char* config_path, char* nombre_interfaz);
void ejecutar_stdin(uint32_t pid, uint32_t tamaño, int socket_kernel);
void ejecutar_sleep(uint32_t pid, uint32_t milisegundos, int socket_kernel);

void* hilo_instruccion_io(void* args_void);

#endif