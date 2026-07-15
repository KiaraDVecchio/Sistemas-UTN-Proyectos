#ifndef PROCESO_H_
#define PROCESO_H_

#include <stdint.h>
#include <stdbool.h>
#include <pthread.h>
#include <commons/collections/list.h>
#include <commons/log.h>

typedef enum {
    NEW,
    READY,
    EXEC,
    BLOCK,
    SUSP_BLOCK,
    SUSP_READY,
    EXIT
} t_estado;

// Guarda los parámetros de la IO que un proceso está esperando.
// Necesario para STDIN: cuando el texto vuelve del módulo IO,
// hay que saber en qué dirección lógica escribirlo en memoria.
typedef struct {
    uint32_t dir_logica;  // dirección donde leer/escribir (STDIN/STDOUT)
    uint32_t tamanio;     // cantidad de bytes (STDIN/STDOUT)
} t_io_pendiente;

// PCB
typedef struct {
    uint32_t pid;
    t_estado estado;
    int prioridad;
    int prioridad_original;
    char* path_proceso;
    uint32_t pc;
    int fd_cpu;
    t_io_pendiente io_pendiente;  // NUEVO: params de la IO en curso
    void* stdin_pendiente;        // buffer leido de STDIN, pendiente de escribir en memoria si el proceso se suspendio esperandolo
    uint32_t stdin_pendiente_tam;
    // MEM_ALLOC que no entró ni compactando: el proceso queda BLOCK hasta que se
    // libere memoria en algún lado (MEM_FREE, fin de proceso, nuevo memory stick, etc).
    bool esperando_mem_alloc;
    uint32_t id_seg_pendiente;
    uint32_t tamanio_pendiente;
} t_proceso;

// Variables Globales
extern t_list* cola_ready;
extern t_list* cola_block;
extern t_list* cola_susp_ready;
extern t_list* procesos;
extern t_list* cpus_libres;
extern t_list** colas_ready_multinivel;
extern int cant_colas;
extern char** algoritmo_por_cola;
extern bool queue_preemption;

// Mutexes
extern pthread_mutex_t mutex_estado;
extern pthread_mutex_t mutex_block;
extern pthread_cond_t  cond_planificador;

// Config
extern char* algoritmo_de_planificacion;
extern uint32_t quantum;
extern uint32_t suspension_timeout;

// Logger
extern t_log* logger;

// Funciones
t_proceso*  crear_proceso(char* path, int prioridad);
void  cambiar_estado(t_proceso* proceso, t_estado nuevo);
void cambiar_prioridad(t_proceso* proceso, int nueva_prioridad);
const char* nombre_estado(t_estado estado);
t_proceso* buscar_proceso_por_pid(uint32_t pid);
void agregar_primero_ready(t_proceso*);
void iniciar_timer_suspension(t_proceso* proceso);

#endif /* PROCESO_H_ */