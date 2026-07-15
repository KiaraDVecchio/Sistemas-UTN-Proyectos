#ifndef MUTEX_HANDLER_H_
#define MUTEX_HANDLER_H_

#include <stdint.h>
#include <stdbool.h>
#include <commons/collections/list.h>
#include "proceso.h"
#include "planificador.h"

typedef struct {
    char nombre[50];
    uint32_t pid_tomador;  
    t_list* cola_espera;
} t_mutex;

void mutex_inicializar(void);

void mutex_create(t_proceso* proceso, char* nombre, t_cpu* cpu);
void mutex_lock(t_proceso* proceso, char* nombre, t_cpu* cpu);
void mutex_unlock(t_proceso* proceso, char* nombre, t_cpu* cpu);

// llamada cuando un proceso termina, libera los mutex que tenia tomados
void mutex_liberar_de_proceso(t_proceso* proceso);

#endif
