#include "cpu_handler.h"

void enviar_proceso_a_cpu(int socket_dispatch, uint32_t pid) {
    // Usamos un OpCode genérico para avisarle a la CPU que le llega un proceso
    t_paquete* paquete = crear_paquete(); 
    paquete->codigo_operacion = DESPACHAR_PROCESO;
    
    agregar_a_paquete(paquete, &pid, sizeof(uint32_t));
    
    enviar_paquete(paquete, socket_dispatch);
    eliminar_paquete(paquete);
    
    log_info(logger, "PID %d enviado a CPU para ejecución.", pid);
}


t_desalojo recibir_desalojo_de_cpu(int socket_dispatch, uint32_t* pid) {
    t_desalojo boleta;
    memset(&boleta, 0, sizeof(t_desalojo));


    int op_code_red = recibir_operacion(socket_dispatch);
    
    if (op_code_red != DESALOJO_CPU) { // Usá el enum que tengas
        log_error(logger, "Se esperaba DESALOJO_CPU pero llegó: %d", op_code_red);
        return boleta;
    }


    t_list* paquete = recibir_paquete(socket_dispatch);


    *pid = *(uint32_t*)list_get(paquete, 0);


    boleta.motivo = *(t_codigo_instruccion*)list_get(paquete, 1);


    switch (boleta.motivo) {
        
        case SLEEP:

            boleta.parametro_1 = *(uint32_t*)list_get(paquete, 2);
            break;

        case MUTEX_LOCK:
        case MUTEX_UNLOCK:
        case MUTEX_CREATE:
            strcpy(boleta.parametro_string, (char*)list_get(paquete, 3));
            break;

        case EXIT_P:
            break;
        case SEGMENTATION_FAULT:
        
            break;
        

        default:
            log_warning(logger, "Motivo %d sin parámetros adicionales procesados.", boleta.motivo);
            break;
    }

    // 6. Fundamental para evitar que se te inunde la RAM (Memory Leak)
    list_destroy_and_destroy_elements(paquete, free);
    
    return boleta;
}


void probar_conexion_con_cpu() {
    logger = log_create("kernel.log", "KERNEL", true, LOG_LEVEL_INFO);
    log_info(logger, "--- INICIANDO PRUEBA DE DISPATCH CON CPU ---");



    char* puerto = "8000";
    
    int server_dispatch = iniciar_servidor(puerto);
    log_info(logger, "Kernel escuchando en:%s. Esperando a la CPU...",  puerto);

    int socket_cpu = esperar_cliente(server_dispatch);
    log_info(logger, "¡CPU Conectada al Dispatch!");

    log_info(logger, "Esperando a que la CPU pida un proceso...");
    int op_code_pedido = recibir_operacion(socket_cpu);
    log_info(logger, "La CPU mandó el OpCode inicial: %d. ¡Tubo limpio!", op_code_pedido);

    uint32_t pid_prueba = 1;
    log_info(logger, "Enviando PID %d a ejecutar...", pid_prueba);
    enviar_proceso_a_cpu(socket_cpu, pid_prueba);


    log_info(logger, "Esperando desalojo de la CPU...");
    
    uint32_t pid = -1;
    t_desalojo boleta = recibir_desalojo_de_cpu(socket_cpu, &pid);

    log_info(logger, "--- RESULTADO DEL DESALOJO ---");
    log_info(logger, "PID devuelto: %d", pid);
    log_info(logger, "Motivo (OpCode): %d", boleta.motivo);
    log_info(logger, "Parámetro 1: %d", boleta.parametro_1);
    log_info(logger, "Parámetro 2: %d", boleta.parametro_2);
    log_info(logger, "Parámetro String: %s", boleta.parametro_string);
    log_info(logger, "------------------------------");


    liberar_conexion(socket_cpu);
    liberar_conexion(server_dispatch);
    log_info(logger, "Prueba finalizada con éxito.");
}

void enviar_interrupcion(int fd_interrupt, uint32_t pid_interrumpir){
    t_paquete* paquete = crear_paquete();
    paquete->codigo_operacion = INTERRUPCION_QUANTUM;
    agregar_a_paquete(paquete, &pid_interrumpir, sizeof(uint32_t));

    enviar_paquete(paquete, fd_interrupt);
    eliminar_paquete(paquete);
    log_info(logger, "Se envio interrupt de proceso %d",pid_interrumpir);
}