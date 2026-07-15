#ifndef UTILS_SOCKETS_H_
#define UTILS_SOCKETS_H_

#include <sys/socket.h>
#include <netdb.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <commons/collections/list.h>
#include <sys/types.h>
#include <arpa/inet.h>
#include <pthread.h>

typedef enum {
    MENSAJE = 0,
    PAQUETE = 1,
    HANDSHAKE_CPU = 2,
    HANDSHAKE_IO = 3,
    HANDSHAKE_KERNEL = 4,
    HANDSHAKE_SWAP = 5,
    HANDSHAKE_MEMORY_STICK = 6,
    // checkpoint-1
    //CPU - MemeoryStick
    ESCRITURA_MEMORIA = 7,
    LECTURA_MEMORIA =8,
    RESPUESTA_OK=9,
    DATOS_LECTURA=10,

    SOLICITUD_INSTRUCCION = 20, // CPU -> Memoria
    RESPUESTA_INSTRUCCION = 21, // Memoria -> CPU
    CREAR_PROCESO = 22,         // Kernel -> Memoria
    FINALIZAR_PROCESO = 23,     // Kernel -> Memoria
    SOLICITUD_IO = 24,          // Kernel -> IO
    
    IO_STDIN= 25,
    IO_STDOUT= 26,
    IO_SLEEP= 27,

    //Agrego esto:
    /*
    DESPACHAR_PROCESO = 7,      
    INTERRUPCION_QUANTUM = 8,  SS
    PROCESO_EXIT = 9,           
    FIN_QUANTUM = 10,           
    SOLICITUD_IO = 11,          
    IO_FINALIZADA = 12    */ 

    //Scheduler <-> cpu, planificacion
    HANDSHAKE_CPU_INTERRUPT = 28,
    DESPACHAR_PROCESO = 29, //ejecutar proceso
    INTERRUPCION_QUANTUM = 30, //terminar ejecucion por fin de quantum
    PROCESO_EXIT = 31, //terminar proceso
    FIN_QUANTUM = 32,                     
    IO_FINALIZADA = 33, //termino la io
    SOLICITUD_CONTEXTO=34,
    CONTEXTO_RESPUESTA =35,
    ACTUALIZAR_CONTEXTO = 36,
    DESALOJO_CPU = 37,
    SOLICITUD_COMPACTACION = 38, // Memoria -> Scheduler
    EJECUTAR_COMPACTACION = 39,  // Scheduler -> Memoria
    FIN_COMPACTACION = 40 ,       // Memoria -> Scheduler

    // --- AGREGADOS PARA COMUNICACIÓN CPU-MEMORY_STICK ---
    HANDSHAKE_CPU_UPDATES = 41, // Conexión del socket dedicado de CPU a Memoria
    NUEVO_MEMORY_STICK = 42 ,    // Push de Memoria a CPU avisando un nuevo MS

    ESCRITURA_BLOQUE = 43,
    LECTURA_BLOQUE = 44,
    SUSPENDER_PROCESO = 45,
    SUSPENSION_OK = 46,
    DESUSPENDER_PROCESO = 47,
    DESUSPENSION_OK = 48,
    TABLA_INICIAL_MS = 49,

    //NOTIFICACIONES DE MEMORY STICKS A SCHEDULER
    MEMORIA_AMPLIADA = 50,
    MEMORIA_CORRUPTA = 51,
    SIN_ESPACIO_DESUSPENSION = 52,

    RESPUESTA_ERROR=53,

    SIN_ESPACIO_MEMORIA = 54 // Memoria -> Scheduler: no entra el segmento ni compactando

} t_cod_op;

typedef struct {
    uint32_t PC;
    uint8_t  AX;
    uint8_t  BX;
    uint8_t  CX;
    uint8_t  DX;
    uint32_t EAX; 
    uint32_t EBX;
    uint32_t ECX;
    uint32_t EDX;
    uint32_t SI;
    uint32_t DI;
} t_registros;

typedef struct {
    uint32_t id;
    uint32_t base;
    uint32_t limite;
} t_segmento;

typedef struct {
    uint32_t     pid;
    t_registros  registros;
    t_list*      tabla_segmentos;
    t_list*      instrucciones;   /* líneas del .bt precargadas en RAM (solo Kernel Memory) */
} t_contexto_proceso;


// Estructura para pedir una instrucción (Fetch)
typedef struct {
    uint32_t pid;
    uint32_t pc;
} t_solicitud_instruccion;

typedef struct {
    int size;
    void* stream;
} t_buffer;

typedef struct {
    t_cod_op codigo_operacion;
    t_buffer* buffer;
} t_paquete;

typedef struct {
    int fd;
    uint32_t id;
    uint32_t tamano;
    uint32_t base_global;
    uint32_t limite_global;
    char* ip;
    char* puerto;
    pthread_mutex_t mutex_fd; /* serializa send()+recv() sobre fd entre distintos clientes (CPU/compactación) */
} t_memory_stick;
//CPU
typedef enum {
    NOOP, SET, MOV_IN, MOV_OUT, SUM, SUB, JNZ, COPY_MEM,
    // Syscalls
    MUTEX_CREATE, MUTEX_LOCK, MUTEX_UNLOCK,
    MEM_ALLOC, MEM_FREE, SLEEP,
    STDOUT, STDIN, INIT_PROC, EXIT_P,
    // Errores
    INSTRUCCION_DESCONOCIDA,
    SEGMENTATION_FAULT
} t_codigo_instruccion;

typedef struct {
    t_codigo_instruccion motivo; 
    uint32_t parametro_1;        
    uint32_t parametro_2;        
    char parametro_string[50]; 
} t_desalojo;

// Cliente 
int crear_conexion(char* ip, char* puerto);
void liberar_conexion(int socket_cliente);

//Servidor
int iniciar_servidor(char* puerto);
int esperar_cliente(int socket_servidor);

//Enviar
void enviar_mensaje(char* mensaje, int socket_cliente);
t_paquete* crear_paquete(void);
void agregar_a_paquete(t_paquete* paquete, void* valor, int tamanio);
void enviar_paquete(t_paquete* paquete, int socket_cliente);
void eliminar_paquete(t_paquete* paquete);

//Recibir
int recibir_operacion(int socket_cliente);
char* recibir_mensaje(int socket_cliente);
t_list* recibir_paquete(int socket_cliente);

//Serializar
void* serializar_paquete(t_paquete* paquete, int bytes);


//CPU

void enviar_contexto(int socket_destino, int op_code, t_contexto_proceso* contexto);
t_contexto_proceso* recibir_contexto(int socket_origen);
t_contexto_proceso* pedir_contexto(int fd, uint32_t pid);
/*
void enviar_contexto(int socket_destino, int op_code, uint32_t pid, t_registros contexto);
t_registros recibir_contexto(int socket_origen, uint32_t* pid_recibido);
t_registros pedir_contexto(int fd,uint32_t pid);
*/

#endif /* UTILS_SOCKETS_H_ */