#ifndef PLANIFICADOR_H_
#define PLANIFICADOR_H_

#include <stdint.h>
#include "proceso.h"
#include <stdbool.h>

//la cpu tiene q tener 2 canales para que no traten de hablar al mismo tiempo y se pisen
typedef struct {
    uint32_t id;
    int fd_dispatch;  //CPU → Scheduler : manejo de procesos
    int fd_interrupt; //Scheduler → CPU : interrupciones de quantum
    bool libre;
    uint32_t pid_actual;
    int prioridad_proceso;
} t_cpu;

void iniciar_planificador(void);

void registrar_cpu_dispatch(int fd);
void registrar_cpu_interrupt(int fd);

//cpu ya con las 2 conexiones esta lista para recibir procesos
void cpu_disponible(t_cpu* cpu);

extern t_list* todas_las_cpus;
void verificar_desalojo_por_prioridad(t_proceso* nuevo_proceso);

#endif /* PLANIFICADOR_H_ */
