#include "kernel_memory.h"


static bool ordenar_por_base(void* a, void* b) {
    return ((t_segmento*)a)->base < ((t_segmento*)b)->base;
}


static bool segmento_esta_en_swap(uint32_t pid, uint32_t id_segmento) {
    char* clave = string_from_format("%u_%u", pid, id_segmento);
    pthread_mutex_lock(&mutex_swap);
    bool esta = dictionary_has_key(mapa_bloques_swap, clave);
    pthread_mutex_unlock(&mutex_swap);
    free(clave);
    return esta;
}

static t_list* recolectar_todos_los_segmentos(void) {
    t_list* resultado = list_create();

    t_list* claves = dictionary_keys(procesos);
    for (int i = 0; i < list_size(claves); i++) {
        char* key               = list_get(claves, i);
        t_contexto_proceso* ctx = dictionary_get(procesos, key);
        if (ctx != NULL && ctx->tabla_segmentos != NULL) {
            for (int j = 0; j < list_size(ctx->tabla_segmentos); j++) {
                t_segmento* seg = list_get(ctx->tabla_segmentos, j);
                if (!segmento_esta_en_swap(ctx->pid, seg->id)) {
                    list_add(resultado, seg);
                }
            }
        }
    }
    list_destroy(claves); /* las claves son del diccionario, NO liberar con free */

    return resultado;
}

void ejecutar_algoritmo_compactacion(void) {

    
    pthread_mutex_lock(&mutex_procesos);
    t_list* segmentos = recolectar_todos_los_segmentos();
    pthread_mutex_unlock(&mutex_procesos);

    if (list_size(segmentos) == 0) {
        log_info(logger, "Compactación: no hay segmentos activos, nada que mover.");
        list_destroy(segmentos);

        pthread_mutex_lock(&mutex_huecos);
        list_clean_and_destroy_elements(huecos_libres, free);
        if (memoria_total_sistema > 0) {
            t_hueco* hueco_total  = malloc(sizeof(t_hueco));
            hueco_total->base     = 0;
            hueco_total->tamano   = memoria_total_sistema;
            hueco_total->limite   = memoria_total_sistema - 1;
            list_add(huecos_libres, hueco_total);
        }
        pthread_mutex_unlock(&mutex_huecos);
        return;
    }

    
    list_sort(segmentos, ordenar_por_base);

    pthread_mutex_lock(&mutex_lista_ms);

    uint32_t nueva_base = 0;

    for (int i = 0; i < list_size(segmentos); i++) {
        t_segmento* seg    = list_get(segmentos, i);
        uint32_t    tamano = seg->limite - seg->base + 1; /* tamaño real */

        if (seg->base != nueva_base) {
            void* datos = leer_memoria_fisica(seg->base, tamano);

            if (datos != NULL) {
                escribir_memoria_fisica(nueva_base, tamano, datos);
                free(datos);

                log_info(logger,
                    "Compactación: Segmento ID %u movido — "
                    "Base anterior: %u → Nueva base: %u | Tamaño: %u",
                    seg->id, seg->base, nueva_base, tamano);
            } else {
                log_error(logger,
                    "Compactación: fallo de lectura en base %u, tamaño %u. "
                    "Segmento ID %u queda en su posición original.",
                    seg->base, tamano, seg->id);
                nueva_base += tamano;
                continue;
            }

            /* Actualizar la tabla de segmentos del proceso dueño.
             * seg->limite = nueva_base + tamano - 1  (fórmula del enunciado)
             */
            seg->base   = nueva_base;
            seg->limite = nueva_base + tamano - 1;
        }

        nueva_base += tamano;
    }

    pthread_mutex_unlock(&mutex_lista_ms);

    list_destroy(segmentos); /* sólo la lista contenedora; los t_segmento* viven en sus contextos */

    
    pthread_mutex_lock(&mutex_huecos);

    list_clean_and_destroy_elements(huecos_libres, free);

    if (nueva_base < memoria_total_sistema) {
        t_hueco* hueco_final  = malloc(sizeof(t_hueco));
        hueco_final->base     = nueva_base;
        hueco_final->tamano   = memoria_total_sistema - nueva_base;
        hueco_final->limite   = memoria_total_sistema - 1;
        list_add(huecos_libres, hueco_final);

        log_info(logger,
            "Compactación: hueco libre resultante — Base: %u | Tamaño: %u | Límite: %u",
            hueco_final->base, hueco_final->tamano, hueco_final->limite);
    } else {
        log_warning(logger, "Compactación: memoria totalmente ocupada, sin hueco libre.");
    }

    pthread_mutex_unlock(&mutex_huecos);
}
