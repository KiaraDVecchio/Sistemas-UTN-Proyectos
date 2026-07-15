#include "mmu.h"


t_dir_logica mmu_traduccion(uint32_t logica_cruda, uint32_t max_segment_size){
    t_dir_logica traduccion;
    traduccion.num_segmento = logica_cruda / max_segment_size;
   traduccion.desplazamiento = logica_cruda % max_segment_size;
   return traduccion;
}

t_segmento* buscar_segmento(t_list* tabla_segmentos, uint32_t num_segmento){
    if (tabla_segmentos == NULL) {
        printf("ERROR CRÍTICO: La tabla de segmentos es NULL\n");
        return NULL;
    }
   
    int cant = list_size(tabla_segmentos);
    
    
    for(int i=0;i<list_size(tabla_segmentos); i++){
        
        t_segmento* seg = list_get(tabla_segmentos,i);
        if (seg == NULL) {
            continue;
        }

        if(seg->id == num_segmento){
            return seg;
        }
    }
   return NULL;

}

uint32_t mmu_calculo_fisica(uint32_t dir_logica_cruda, uint32_t n_bytes, t_list* tabla_segmentos, uint32_t max_segment_size){
    t_dir_logica dir = mmu_traduccion(dir_logica_cruda, max_segment_size);
    t_segmento* segmento = buscar_segmento(tabla_segmentos, dir.num_segmento);
    if(segmento == NULL){
        return -1; 
    }
    uint32_t tam_segmento = segmento->limite - segmento->base+1;

    if((dir.desplazamiento+n_bytes)>tam_segmento){
        return -2;
    }
    return segmento->base + dir.desplazamiento;
}





