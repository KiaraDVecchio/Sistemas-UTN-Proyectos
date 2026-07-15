#include "kernel_memory.h"
#include <pthread.h>

t_log* logger;
t_config* config;
t_dictionary* procesos;
pthread_mutex_t mutex_procesos = PTHREAD_MUTEX_INITIALIZER;
int fd_kernel = -1;
t_list* lista_fd_cpu_updates;
pthread_mutex_t mutex_cpu_updates = PTHREAD_MUTEX_INITIALIZER;
t_list* lista_memory_sticks;
uint32_t memoria_total_sistema = 0;
t_list* huecos_libres;
uint32_t tamano_bloque_swap = 0;
uint32_t tamano_total_swap = 0;
t_bitarray* bitmap_swap;
void* puntero_bitmap;
int fd_swap = -1;
t_dictionary* mapa_bloques_swap = NULL;
pthread_mutex_t mutex_huecos = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t mutex_swap = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t mutex_lista_ms = PTHREAD_MUTEX_INITIALIZER;


int iniciar_memoria(char* config_path) {
    config = config_create(config_path);
    char* log_level_str = config_get_string_value(config, "LOG_LEVEL");
    logger = log_create("memory.log", "MEMORY", true, log_level_from_string(log_level_str));

    huecos_libres = list_create();
    procesos = dictionary_create();
    lista_memory_sticks = list_create();
    lista_fd_cpu_updates = list_create();
    mapa_bloques_swap = dictionary_create();
    
    pthread_mutex_init(&mutex_procesos, NULL);

    char* puerto = config_get_string_value(config, "PUERTO_ESCUCHA");
    int server_fd = iniciar_servidor(puerto);

    log_info(logger, "Servidor Kernel Memory escuchando en el puerto %s", puerto);

    atender_clientes_memoria(server_fd);

    pthread_mutex_destroy(&mutex_procesos);
    
    return 0;

}

t_hueco* buscar_hueco_libre(uint32_t tamano_segmento) {
    char* algoritmo = config_get_string_value(config, "ALLOCATION_STRATEGY");
    t_hueco* hueco_elegido = NULL;

    for (int i = 0; i < list_size(huecos_libres); i++) {
        t_hueco* hueco_actual = list_get(huecos_libres, i);

        if (hueco_actual->tamano >= tamano_segmento) {
                        if (hueco_elegido == NULL) {
                hueco_elegido = hueco_actual;
            } else {
                if (strcmp(algoritmo, "BEST") == 0) {
                    if (hueco_actual->tamano < hueco_elegido->tamano) {
                        hueco_elegido = hueco_actual;
                    }
                } else if (strcmp(algoritmo, "WORST") == 0) {
                    if (hueco_actual->tamano > hueco_elegido->tamano) {
                        hueco_elegido = hueco_actual;
                    }
                }
            }
        }
    }

    if (hueco_elegido != NULL) {
        log_info(logger, "Hueco elegido para %u bytes -> Base: %u, Tamaño original: %u (Algoritmo: %s)", 
                 tamano_segmento, hueco_elegido->base, hueco_elegido->tamano, algoritmo);
    }

    return hueco_elegido;
}
bool hueco_menor_base(void* a, void* b) {
    return ((t_hueco*)a)->base < ((t_hueco*)b)->base;
}

void consolidar_huecos(void) {
    if (list_size(huecos_libres) < 2) return;

    // Ordenar los huecos por dirección de base
    list_sort(huecos_libres, hueco_menor_base);

    int i = 0;
    while (i < list_size(huecos_libres) - 1) {
        t_hueco* actual = list_get(huecos_libres, i);
        t_hueco* siguiente = list_get(huecos_libres, i + 1);

        // Si están pegados, se fusionan
        if ((actual->base + actual->tamano) == siguiente->base) {
            actual->tamano += siguiente->tamano;
            actual->limite = siguiente->limite;
            
            list_remove(huecos_libres, i + 1);
            free(siguiente);
        } else {
            i++;
        }
    }
}