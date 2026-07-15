#ifndef KERNEL_MEMORY_H_
#define KERNEL_MEMORY_H_

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <commons/log.h>
#include <commons/config.h>
#include <commons/string.h>
#include <commons/collections/dictionary.h>
#include "conexiones.h" 
#include <pthread.h>
#include <commons/bitarray.h>

extern t_log* logger;
extern t_config* config;
extern t_dictionary* procesos;
extern pthread_mutex_t mutex_procesos;
extern int fd_kernel;
extern t_list* lista_fd_cpu_updates;
extern pthread_mutex_t mutex_cpu_updates;
extern t_list* lista_memory_sticks;
extern pthread_mutex_t mutex_lista_ms; 
extern uint32_t memoria_total_sistema;
extern t_list* huecos_libres;
extern pthread_mutex_t mutex_huecos; 
extern uint32_t tamano_bloque_swap;
extern uint32_t tamano_total_swap;
extern t_bitarray* bitmap_swap;
extern void* puntero_bitmap;
extern t_dictionary* mapa_bloques_swap;
extern int fd_swap;
extern pthread_mutex_t mutex_swap;

typedef struct {
    uint32_t base;
    uint32_t limite;
    uint32_t tamano;
} t_hueco;

// Prototipos de funciones
t_memory_stick* buscar_ms_por_direccion(uint32_t dir_fisica, uint32_t* dir_local);
void desconectar_memory_stick(int fd);
t_hueco*        buscar_hueco_libre(uint32_t tamano_segmento);

int  iniciar_memoria(char* config_path);
void atender_clientes_memoria(int server_fd);

/* Fetch: acceso O(1) a la lista precargada en el contexto del proceso */
char*   obtener_linea_de_archivo(uint32_t pid, uint32_t pc);
/* Preload: lee el archivo de instrucciones UNA sola vez y devuelve t_list* de char* */
t_list* cargar_instrucciones_desde_archivo(uint32_t pid, char* path_recibido);
/* Fase 0 de des-suspensión: verifica sin modificar estado que todos los segmentos caben */
bool    verificar_espacio_para_desuspension(t_list* tabla_segmentos);

void* leer_memoria_fisica(uint32_t dir_fisica, uint32_t tamanio);
bool  escribir_memoria_fisica(uint32_t dir_fisica, uint32_t tamanio, void* datos);
void ejecutar_algoritmo_compactacion(void);
bool suspender_segmento(uint32_t pid, t_segmento* seg, int fd_swap);
bool desuspender_segmento(uint32_t pid, t_segmento* seg, int fd_swap);
/* Reserva base/limite para un segmento; asume mutex_huecos ya tomado por el caller.
 * Devuelve false si no hay hueco disponible (sin modificar el estado de huecos_libres). */
bool reservar_hueco_para_segmento(t_segmento* seg, uint32_t tamano_seg);
/* Deshace una reserva previa devolviendo el espacio a huecos_libres; asume mutex_huecos tomado. */
void liberar_hueco_reservado(t_segmento* seg, uint32_t tamano_seg);
void consolidar_huecos(void);
bool hueco_menor_base(void* a, void* b);
void liberar_bloques_swap(uint32_t pid, uint32_t id_segmento);
void liberar_huecos_de_proceso(t_contexto_proceso* ctx);

#endif