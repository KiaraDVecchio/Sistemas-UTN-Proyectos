#include "../utils/src/sockets.h"
#include <stdio.h>

int main() {
    printf("[PRUEBA] Conectando a Kernel Memory...\n");
    int fd = crear_conexion("127.0.0.1", "8001");
    if (fd == -1) {
        printf("[ERROR] No se pudo conectar. ¿Está prendida la Memoria?\n");
        return -1;
    }
    
    // 1. Conectar como Kernel
    int op = HANDSHAKE_KERNEL;
    send(fd, &op, sizeof(int), 0);

    // 2. Crear Proceso PID 99
    printf("[PRUEBA] Mandando CREAR_PROCESO para PID 99...\n");
    t_paquete* paq = crear_paquete();
    paq->codigo_operacion = CREAR_PROCESO;
    uint32_t pid = 99;
    agregar_a_paquete(paq, &pid, sizeof(uint32_t));
    enviar_paquete(paq, fd);
    eliminar_paquete(paq);
    
    int resp;
    recv(fd, &resp, sizeof(int), MSG_WAITALL); 
    if (resp == RESPUESTA_OK) printf("[PRUEBA] ¡Proceso creado OK!\n");

    // 3. Suspender Proceso
    printf("[PRUEBA] Mandando SUSPENDER_PROCESO...\n");
    op = SUSPENDER_PROCESO;
    send(fd, &op, sizeof(int), 0);
    send(fd, &pid, sizeof(uint32_t), 0);
    
    recv(fd, &resp, sizeof(int), MSG_WAITALL); 
    if (resp == SUSPENSION_OK) printf("[PRUEBA] ¡Proceso SUSPENDIDO OK! (Revisá la consola de SWAP)\n");

    // 4. Desuspender Proceso
    printf("[PRUEBA] Mandando DESUSPENDER_PROCESO...\n");
    op = DESUSPENDER_PROCESO;
    send(fd, &op, sizeof(int), 0);
    send(fd, &pid, sizeof(uint32_t), 0);
    
    recv(fd, &resp, sizeof(int), MSG_WAITALL); 
    if (resp == DESUSPENSION_OK) printf("[PRUEBA] ¡Proceso DES-SUSPENDIDO OK! (Revisá la consola de SWAP)\n");

    printf("[PRUEBA] Test finalizado con éxito.\n");
    return 0;
}