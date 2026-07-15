#ifndef CPU_MMU_H_
#define CPU_MMU_H_

#include <stdint.h>
#include <stdbool.h>
#include <commons/log.h>
#include "sockets.h"


typedef struct {
    uint32_t num_segmento;
    uint32_t desplazamiento;
} t_dir_logica;



t_dir_logica mmu_traduccion(uint32_t logica_cruda, uint32_t max_segment_size);
t_segmento* buscar_segmento(t_list* tabla_segementos, uint32_t num_segmento);
uint32_t mmu_calculo_fisica(uint32_t dir_logica_cruda, uint32_t n_bytes, t_list* tabla_segmentos, uint32_t max_segment_size);


#endif