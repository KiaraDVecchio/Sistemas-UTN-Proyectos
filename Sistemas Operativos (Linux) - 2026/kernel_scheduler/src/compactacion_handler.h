#ifndef COMPACTACION_HANDLER_H_
#define COMPACTACION_HANDLER_H_

void iniciar_escucha_kernel_memory(void);
void ejecutar_compactacion(void);
void intentar_dessuspender(void);
void intentar_reintentar_mem_alloc_pendientes(void);

#endif
