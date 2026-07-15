#ifndef CPU_COMUNICACION_H_
#define CPU_COMUNICACION_H_

#include <stdint.h>
#include <stdbool.h>
#include <pthread.h>
#include <commons/log.h>
#include<pthread.h>
#include "sockets.h"


extern t_log* logger;
extern t_list* tabla_memory_sticks;
extern pthread_mutex_t mutex_sticks;



typedef struct {
    t_codigo_instruccion op_code;
    

    uint8_t* reg_dest_8;
    uint32_t* reg_dest_32;
    
    uint8_t* reg_orig_8;
    uint32_t* reg_orig_32;
    

    uint32_t valor_numerico;
    uint32_t size;

    char parametro_string[50]; 
    
} t_instruccion_decodificada;


typedef struct {
    uint32_t dir_fisica_global; 
    uint32_t tam_a_operar;   
    int socket_destino;       
} t_peticion_stick;

extern int socket_memoria;   
extern int socket_dispatch;    
extern int socket_interrupt;   
extern int socket_ms;         



bool conectar_memory_stick(char* ip, char* puerto);


bool solicitar_escritura_ms(uint32_t dir_fisica, uint32_t tam_total, void* datos, bool* fue_error_comunicacion);


void* solicitar_lectura_ms(uint32_t dir_fisica, uint32_t tam_total, bool* fue_error_comunicacion);

void avisar_finalizacion_al_scheduler(uint32_t pid, t_desalojo boleta);

bool fragmentar_cubre_todo(t_list* peticiones, uint32_t tam_total);
uint32_t esperaProceso();

char* solicitar_proxima_instruccion(uint32_t pid, uint32_t pc);
int cpu_conexion(char* ip, char* puerto, int cod_op, uint32_t id_emisor);

uint32_t recibir_interrupcion();

t_memory_stick buscar_MS(uint32_t dir_fisica);
t_list* fragmentar_peticion(uint32_t dir_fisica_inicial, uint32_t tam_total);
#endif 