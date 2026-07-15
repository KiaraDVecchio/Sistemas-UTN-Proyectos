#include "comunicacion.h"

int socket_ms = -1;
int socket_memoria = -1;     
int socket_dispatch = -1;    
int socket_interrupt = -1;   
extern int fd_kernel;
extern int fd_memoria;
extern int fd_interrupt;

t_memory_stick buscar_MS(uint32_t dir_fisica){
    pthread_mutex_lock(&mutex_sticks);
    for(int i=0; i<list_size(tabla_memory_sticks);i++){
        t_memory_stick* stick = list_get(tabla_memory_sticks,i);
        if(dir_fisica>=stick->base_global && dir_fisica<= stick->limite_global){
            t_memory_stick copia= *stick;
            pthread_mutex_unlock(&mutex_sticks);
            return copia;
        }
    }
    pthread_mutex_unlock(&mutex_sticks);
    t_memory_stick error;
    error.fd = -1;
    return error;
}

bool fragmentar_cubre_todo(t_list* peticiones, uint32_t tam_total){
    uint32_t total = 0;
    for(int i=0; i<list_size(peticiones);i++){
        t_peticion_stick* p =list_get(peticiones,i);
        total += p->tam_a_operar;
    }
    return total == tam_total;
}

t_list* fragmentar_peticion(uint32_t dir_fisica_inicial, uint32_t tam_total){
    t_list* peticiones = list_create();
    uint32_t bytes_listos =0;
    uint32_t dir_fisica_actual = dir_fisica_inicial;

    

    while(bytes_listos< tam_total){
        t_memory_stick stick_actual = buscar_MS(dir_fisica_actual);
        if(stick_actual.fd == -1){
            break; //error por falta de memorystick
        }
        
        uint32_t offset = dir_fisica_actual - stick_actual.base_global;
        uint32_t espacio_disponible = stick_actual.tamano - offset;
        uint32_t bytes_restantes = tam_total-bytes_listos;

        uint32_t tam_operacion = (bytes_restantes < espacio_disponible)? bytes_restantes : espacio_disponible;
        
        t_peticion_stick* peticion = malloc(sizeof(t_peticion_stick));
        peticion->dir_fisica_global = dir_fisica_actual-stick_actual.base_global;  
        peticion->tam_a_operar = tam_operacion;
        peticion->socket_destino = stick_actual.fd;
        list_add(peticiones, peticion);
        bytes_listos += tam_operacion;
        dir_fisica_actual += tam_operacion;
    }
    return peticiones;

}

bool solicitar_escritura_ms(uint32_t dir_fisica_inicial, uint32_t tam_total, void* datos_completos, bool* fue_error_comunicacion) {
    if (fue_error_comunicacion) *fue_error_comunicacion = false;

    t_list* peticiones = fragmentar_peticion(dir_fisica_inicial,tam_total);

    if(list_is_empty(peticiones)||!fragmentar_cubre_todo(peticiones,tam_total)){
        log_error(logger, "No se pudo escribir en MS, el tam. de escritura no esta cubierta por ningun ms conectado");
        list_destroy_and_destroy_elements(peticiones,free);
        return false;
    }
    bool ok =true;

    uint32_t offset_datos=0;
    for(int i=0; i<list_size(peticiones);i++){
        t_peticion_stick* peticion = list_get(peticiones,i);
        t_paquete* paquete = crear_paquete();
        paquete->codigo_operacion = ESCRITURA_MEMORIA;

        agregar_a_paquete(paquete, &(peticion->dir_fisica_global),sizeof(uint32_t));
        agregar_a_paquete(paquete, &(peticion->tam_a_operar),sizeof(uint32_t));

        void* porcion_enviar = (char*) datos_completos+offset_datos;
        agregar_a_paquete(paquete, porcion_enviar, peticion->tam_a_operar);

        enviar_paquete(paquete, peticion->socket_destino);
        eliminar_paquete(paquete);
        log_debug(logger,"Solicito Escritura de %u bytes", peticion->tam_a_operar);
        int cod_op = recibir_operacion(peticion->socket_destino);
        if(cod_op == RESPUESTA_OK){
            offset_datos += peticion->tam_a_operar;
        }else {

            if (cod_op == -1) {
                log_error(logger, "Memory Stick desconectado a mitad de una escritura");
                if (fue_error_comunicacion) *fue_error_comunicacion = true;
            } else {
                log_error(logger, "El MS respondio con error");
            }
            ok=false;
        }
        free(peticion);

    }
    list_destroy(peticiones);
    return ok;
}

void* solicitar_lectura_ms(uint32_t dir_fisica_inicial, uint32_t tam_total, bool* fue_error_comunicacion) {
    if (fue_error_comunicacion) *fue_error_comunicacion = false;

    t_list* peticiones = fragmentar_peticion(dir_fisica_inicial,tam_total);

    if(list_is_empty(peticiones)||!fragmentar_cubre_todo(peticiones,tam_total)){
        log_error(logger, "No se pudo leer en MS, el tam. de lectura no esta cubierta por ningun ms conectado");
        list_destroy_and_destroy_elements(peticiones,free);

        return NULL;
    }
    void* buffer = calloc(1, tam_total);
    uint32_t offset=0;

    for(int i=0; i<list_size(peticiones);i++){
        t_peticion_stick* peticion = list_get(peticiones,i);
        t_paquete* paquete = crear_paquete();
        paquete->codigo_operacion = LECTURA_MEMORIA;

        agregar_a_paquete(paquete, &(peticion->dir_fisica_global),sizeof(uint32_t));
        agregar_a_paquete(paquete, &(peticion->tam_a_operar),sizeof(uint32_t));
        enviar_paquete(paquete, peticion->socket_destino);
        eliminar_paquete(paquete);
        int cod_op = recibir_operacion(peticion->socket_destino);

        if(cod_op == DATOS_LECTURA){
            t_list* respuesta = recibir_paquete(peticion->socket_destino);
            void* chunk_leido = list_get(respuesta,0);
            memcpy((char*)buffer + offset, chunk_leido, peticion->tam_a_operar);
            offset += peticion->tam_a_operar;
            log_debug(logger,"Leyo %u",*(uint32_t*)chunk_leido);
            list_destroy_and_destroy_elements(respuesta,free);
        }else{

            if (cod_op == -1) {
                log_error(logger, "Memory Stick desconectado a mitad de una lectura");
                if (fue_error_comunicacion) *fue_error_comunicacion = true;
            } else {
                log_error(logger, "El MS respondio con error a la peticion de lectura");
            }
            free(buffer);
            free(peticion);
            // quedan peticiones sin procesar en la lista: liberarlas junto con la lista
            for (int j = i + 1; j < list_size(peticiones); j++) free(list_get(peticiones, j));
            list_destroy(peticiones);
            return NULL;
        }
        free(peticion);

    }
    list_destroy(peticiones);
    return buffer;
}

bool conectar_memory_stick(char* ip, char* puerto) {
    socket_ms = crear_conexion(ip, puerto);
    if (socket_ms == -1) {
        log_error(logger, "Error al conectar con el Memory Stick.");
        return false;
    }


    uint32_t mi_id_cpu = 1; 
    send(socket_ms, &mi_id_cpu, sizeof(uint32_t), 0);


    int respuesta_handshake;
    recv(socket_ms, &respuesta_handshake, sizeof(int), MSG_WAITALL);

    if (respuesta_handshake != 0) {
        log_error(logger, "El Memory Stick rechazó el Handshake.");
        return false;
    }

    log_debug(logger, "Conectado al Memory Stick exitosamente (Handshake OK).");
    return true;
}


void avisar_finalizacion_al_scheduler(uint32_t pid, t_desalojo boleta) {

    t_paquete* paquete = crear_paquete();
    paquete->codigo_operacion = DESALOJO_CPU;

    agregar_a_paquete(paquete, &pid, sizeof(uint32_t));
    agregar_a_paquete(paquete, &(boleta.motivo), sizeof(t_codigo_instruccion));

    switch (boleta.motivo) {
        case SLEEP:
            agregar_a_paquete(paquete, &(boleta.parametro_1), sizeof(uint32_t));
            break;

        case MUTEX_CREATE:
        case MUTEX_LOCK:
        case MUTEX_UNLOCK: {
            uint32_t largo_string = strlen(boleta.parametro_string) + 1;
            agregar_a_paquete(paquete, &largo_string, sizeof(uint32_t));
            agregar_a_paquete(paquete, boleta.parametro_string, largo_string);
            break;
        }

        case EXIT_P:
        case INTERRUPCION_QUANTUM:
        case SEGMENTATION_FAULT:
            break;

        case STDOUT:
        case STDIN:
            agregar_a_paquete(paquete, &(boleta.parametro_1), sizeof(uint32_t)); 
            agregar_a_paquete(paquete, &(boleta.parametro_2), sizeof(uint32_t)); 
            break;

        case MEM_ALLOC:
            agregar_a_paquete(paquete, &(boleta.parametro_1), sizeof(uint32_t)); 
            agregar_a_paquete(paquete, &(boleta.parametro_2), sizeof(uint32_t)); 
            break;

        case MEM_FREE:
            agregar_a_paquete(paquete, &(boleta.parametro_1), sizeof(uint32_t)); 
            break;

        case INIT_PROC: {
            uint32_t largo = strlen(boleta.parametro_string) + 1;
            agregar_a_paquete(paquete, &largo, sizeof(uint32_t));
            agregar_a_paquete(paquete, boleta.parametro_string, largo);
            agregar_a_paquete(paquete, &(boleta.parametro_1), sizeof(uint32_t));
            break;
        }

        default:
            break;
    }

    enviar_paquete(paquete, fd_kernel);
    eliminar_paquete(paquete);
}



uint32_t esperaProceso() {
    log_info(logger, "Esperando proceso del Kernel en Dispatch...");
    

    int op_code = recibir_operacion(fd_kernel);
    
    if (op_code <= 0) {
        log_error(logger, "Se desconectó el Kernel.");
        exit(EXIT_FAILURE);
    }
    
    // 2. Desempaquetamos
if (op_code == DESPACHAR_PROCESO) {
        int size_payload;

        recv(fd_kernel, &size_payload, sizeof(int), MSG_WAITALL);
        
        int size_dato;

        recv(fd_kernel, &size_dato, sizeof(int), MSG_WAITALL);
        
        uint32_t pid_recibido;

        recv(fd_kernel, &pid_recibido, sizeof(uint32_t), MSG_WAITALL);
        
        return pid_recibido;
    }else {
        log_error(logger, "OpCode inesperado: %d", op_code);
        exit(EXIT_FAILURE);
    }
}

char* solicitar_proxima_instruccion(uint32_t pid, uint32_t pc) {
    t_paquete* paquete = crear_paquete();
    paquete->codigo_operacion = SOLICITUD_INSTRUCCION;
    
    agregar_a_paquete(paquete, &pid, sizeof(uint32_t));
    agregar_a_paquete(paquete, &pc, sizeof(uint32_t));
    
    enviar_paquete(paquete, fd_memoria); 
    eliminar_paquete(paquete);

    int cod_op = recibir_operacion(fd_memoria);
    if (cod_op == RESPUESTA_INSTRUCCION) {
        t_list* respuesta = recibir_paquete(fd_memoria);
        char* instruccion = strdup((char*)list_get(respuesta, 0));
        list_destroy_and_destroy_elements(respuesta, free);
        return instruccion;
    }
    return NULL;
}


int cpu_conexion(char* ip, char* puerto, int cod_op, uint32_t id_emisor) {
    

    int socket_conexion = crear_conexion(ip, puerto);

    if (socket_conexion <= 0) {
        log_error(logger, "Fallo al intentar conectar con el servidor (IP: %s, Puerto: %s)", ip, puerto);
        return -1; 
    }

    send(socket_conexion, &cod_op, sizeof(int), 0);


    send(socket_conexion, &id_emisor, sizeof(uint32_t), 0);

    log_info(logger, "Conexión establecida. Handshake enviado identificándome como ID %u.", id_emisor);


    return socket_conexion;
}


uint32_t recibir_interrupcion(){
    int op_code;
    uint32_t pidInt;
    
    while(1){

        if(recv(fd_interrupt, &op_code, sizeof(int), MSG_WAITALL) <= 0){
            log_error(logger, "Kernel desconectado de Socket Interrupt");
            return -1;
        }
        
        if(op_code == INTERRUPCION_QUANTUM){
            int size_buffer;
            log_info(logger,"## Interrupcion Recibida");
            if(recv(fd_interrupt, &size_buffer, sizeof(int), MSG_WAITALL) <= 0) {
                return -1;
            }

            void* buffer = malloc(size_buffer);
            if(recv(fd_interrupt, buffer, size_buffer, MSG_WAITALL) <= 0) {
                free(buffer);
                return -1;
            }

            int desplazamiento = 0;

            if (size_buffer == 8) {

                desplazamiento = sizeof(int); 
            } else if (size_buffer == 4) {

                desplazamiento = 0; 
            } else {
                log_warning(logger, "Atención: Tamaño de buffer inesperado: %d", size_buffer);
            }
            memcpy(&pidInt, buffer + desplazamiento, sizeof(uint32_t));
            free(buffer);
            
            log_info(logger, "Llego interrupcion de Kernel para el PID %u", pidInt);
            return pidInt;
        }
    }
}