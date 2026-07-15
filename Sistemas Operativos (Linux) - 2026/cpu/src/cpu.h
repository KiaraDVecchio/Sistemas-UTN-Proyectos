#ifndef CPU_H_
#define CPU_H_

#include <commons/log.h>
#include <commons/config.h>
#include <commons/string.h>
#include <stdint.h>
#include "conexiones.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <commons/log.h>
#include "sockets.h"
#include "comunicacion.h"
#include "mmu.h"

extern t_log* logger;
extern t_config* config;



int iniciar_cpu(char* config_path, char* id_cpu_str);
void finalizar_cpu(void);

//Ciclo
void ejecucionDeProceso(void);

t_instruccion_decodificada decodificar(char* instruccion_cruda);
void destruir_instruccion(t_instruccion_decodificada instruccion);
bool ejecutar_instruccion(t_instruccion_decodificada* instruccion, uint32_t pid);


uint32_t* obtener_registro_32(char* nombre);
uint8_t* obtener_registro_8(char* nombre);
t_codigo_instruccion traducir_instruccion(char* operacion);
t_desalojo ciclo_instruccion(uint32_t pid);
void* hilo_escuchar_updates_ms(void* arg);

void* espera_interrupcion();
const char* obtener_nombre_reg_8(uint8_t* puntero_reg);
const char* obtener_nombre_reg_32(uint32_t* puntero_reg);
#endif