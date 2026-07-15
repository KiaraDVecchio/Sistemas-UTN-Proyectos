#include "cpu.h"
#include <pthread.h>

t_log* logger;
t_config* config;
t_registros registros_cpu;
t_contexto_proceso* ctx; 
t_list* tabla_segmentos;
int fd_kernel; 
int fd_memoria;
int fd_interrupt;
int fd_memory_updates;
uint32_t max_segment_size;

t_list* tabla_memory_sticks = NULL;
bool interrupcion_pendiente;
uint32_t pid_a_interrumpir = -1;
pthread_mutex_t mutex_interrupcion;
pthread_mutex_t mutex_sticks;
uint32_t cpu_id;


static uint32_t valor_para_log(void* buffer, uint32_t tamanio_buffer) {
    uint32_t valor = 0;
    uint32_t bytes_a_leer = (tamanio_buffer < sizeof(uint32_t)) ? tamanio_buffer : sizeof(uint32_t);
    memcpy(&valor, buffer, bytes_a_leer);
    return valor;
}


static void abortar_por_falla_infraestructura_ms(uint32_t pid) {
    log_error(logger, "PID: %d - Se perdió la conexión con un Memory Stick a mitad de una operación. No se puede continuar.", pid);
    exit(EXIT_FAILURE);
}

int iniciar_cpu(char* config_path, char* id_cpu_str) { 
    config = config_create(config_path);
    cpu_id = atoi(id_cpu_str);

    char* log_name = string_from_format("cpu_%s.log", id_cpu_str);
    char* log_level_str = config_get_string_value(config, "LOG_LEVEL");
    logger = log_create(log_name, "CPU", true, log_level_from_string(log_level_str));
    
    uint32_t id_cpu = (uint32_t)atoi(id_cpu_str);

    tabla_memory_sticks = list_create();
    pthread_mutex_init(&mutex_sticks, NULL);

    char* ip_kernel= config_get_string_value(config, "IP_KERNEL");
    char* puerto_dispatch = config_get_string_value(config, "PUERTO_KERNEL");
    char* puerto_interrupt = config_get_string_value(config, "PUERTO_KERNEL");
    
    char* ip_m = config_get_string_value(config, "IP_MEMORY");
    char* puerto_m = config_get_string_value(config, "PUERTO_MEMORY");

    //CPU se conecta a Kernel 
    fd_kernel = cpu_conexion(ip_kernel, puerto_dispatch, HANDSHAKE_CPU, cpu_id);
    if(fd_kernel != -1) log_info(logger, "Conectado a Kernel Scheduler");
    fd_interrupt = cpu_conexion(ip_kernel, puerto_interrupt, HANDSHAKE_CPU_INTERRUPT, cpu_id);
    if(fd_interrupt != -1) log_info(logger, "Conectado a Kernel Scheduler Interrupt");
    //CPU se conecta a Memoria
    fd_memoria = conectarse_a_modulo(ip_m, puerto_m, logger, HANDSHAKE_CPU); //dEBERIA DEVOLVERME EL uint32_t tam_max_segmento
    if(fd_memoria != -1) {
            send(fd_memoria, &id_cpu, sizeof(uint32_t), 0);
            log_info(logger, "Conectado a Kernel Memory");
            
            recv(fd_memoria, &max_segment_size, sizeof(uint32_t), MSG_WAITALL); 
            log_info(logger, "Tamaño máximo de segmento recibido: %u", max_segment_size);
        } 
    fd_memory_updates = conectarse_a_modulo(ip_m, puerto_m, logger, HANDSHAKE_CPU_UPDATES);
    if(fd_memory_updates != -1){
        log_info(logger, "Conectando a Memoria Datos de Memorysticks");
        pthread_t hilo_updates;
        pthread_create(&hilo_updates, NULL, hilo_escuchar_updates_ms, NULL);
        pthread_detach(hilo_updates);
    }

    return 0;
}


void finalizar_cpu() {
    log_info(logger, "Apagando modulo CPU...");
    config_destroy(config);
    log_destroy(logger);
}

//Ciclo de ejecucion

void ejecucionDeProceso() {
    while(1) { //Se mantiene el ciclo constantemente
        uint32_t pid_actual; //Deberia ser global
        pid_actual = esperaProceso();
        log_info(logger, "Comienzo ejecución de PID %d", pid_actual);
        log_info(logger, "Pido Contexto de PID %d", pid_actual);

        ctx = pedir_contexto(fd_memoria, pid_actual);
        registros_cpu = ctx->registros;
        tabla_segmentos = ctx->tabla_segmentos;


        log_debug(logger, "--- CONTEXTO RECIBIDO PARA PID %u ---", pid_actual);
        log_debug(logger, "PC: %u", registros_cpu.PC);
        log_debug(logger, "Registros de 8 bits  -> AX: %u | BX: %u | CX: %u | DX: %u", 
                registros_cpu.AX, registros_cpu.BX, registros_cpu.CX, registros_cpu.DX);
        log_debug(logger, "Registros de 32 bits -> EAX: %u | EBX: %u | ECX: %u | EDX: %u", 
                registros_cpu.EAX, registros_cpu.EBX, registros_cpu.ECX, registros_cpu.EDX);
        log_debug(logger, "Registros Índice     -> SI: %u | DI: %u", 
                registros_cpu.SI, registros_cpu.DI);
        log_debug(logger, "-------------------------------------");

        //Entra al ciclo y devuelve porque sale
        t_desalojo motivo = ciclo_instruccion(pid_actual);

        t_contexto_proceso ctx_actualizar;
        ctx_actualizar.pid = pid_actual;
        ctx_actualizar.registros = registros_cpu;
        ctx_actualizar.tabla_segmentos = list_create();
        enviar_contexto(fd_memoria, ACTUALIZAR_CONTEXTO, &ctx_actualizar);
        list_destroy(ctx_actualizar.tabla_segmentos);

        int ack_actualizar_contexto = recibir_operacion(fd_memoria);
        if (ack_actualizar_contexto != RESPUESTA_OK) {
            log_warning(logger, "ACTUALIZAR_CONTEXTO: se esperaba RESPUESTA_OK y llegó OpCode %d", ack_actualizar_contexto);
        }

        log_info(logger, "Envio contexto a Memoria");
        if (ctx->tabla_segmentos) list_destroy_and_destroy_elements(ctx->tabla_segmentos, free);
        free(ctx);
        ctx = NULL;
        tabla_segmentos = NULL;

        avisar_finalizacion_al_scheduler(pid_actual, motivo);
        log_info(logger, "Envio motivo de fin a Kernel");

    }
}


t_desalojo ciclo_instruccion(uint32_t pid){ 
    bool fin_ciclo = false;

    t_desalojo finCiclo;
    memset(&finCiclo, 0, sizeof(t_desalojo));
    finCiclo.motivo = EXIT_P;

    while(!fin_ciclo) {
        //Fetch
        log_info(logger, "## PID: %d - FETCH - Program Counter: %d", pid, registros_cpu.PC);
        char* instruccion_cruda = solicitar_proxima_instruccion(pid, registros_cpu.PC);
        log_info(logger, "Instruccion Recibida: %s", instruccion_cruda);

        registros_cpu.PC++;

        //Decode
        t_instruccion_decodificada instruccion = decodificar(instruccion_cruda);

        //Execute
        
        fin_ciclo = ejecutar_instruccion(&instruccion, pid);
        
        //Check Interrupt
        bool flag_interrupcion = false;

        pthread_mutex_lock(&mutex_interrupcion);
        if(pid_a_interrumpir == pid){
            flag_interrupcion = true;
            pid_a_interrumpir = -1; //No se si es mejor sacarlo afuera del if para que siempre reinicie
        } 
        pthread_mutex_unlock(&mutex_interrupcion);
    
        //Fin ejecucion
        if(fin_ciclo){
                
                finCiclo.motivo = instruccion.op_code;
                finCiclo.parametro_1 = instruccion.valor_numerico;
                finCiclo.parametro_2 = instruccion.size; 
                
                if (instruccion.parametro_string != NULL) {
                    strcpy(finCiclo.parametro_string, instruccion.parametro_string); 
                }
        }else if(flag_interrupcion) {
            log_info(logger, "Fin de ejecucion por interrupt");
            fin_ciclo = true;
            finCiclo.motivo = INTERRUPCION_QUANTUM;
        }


        free(instruccion_cruda); 
    }
    
    return finCiclo; 
}

t_codigo_instruccion traducir_instruccion(char* operacion) {
    if (strcmp(operacion, "NOOP") == 0) return NOOP;
    if (strcmp(operacion, "SET") == 0) return SET;
    if (strcmp(operacion, "MOV_IN") == 0) return MOV_IN;
    if (strcmp(operacion, "MOV_OUT") == 0) return MOV_OUT;
    if (strcmp(operacion, "SUM") == 0) return SUM;
    if (strcmp(operacion, "SUB") == 0) return SUB;
    if (strcmp(operacion, "JNZ") == 0) return JNZ;
    if (strcmp(operacion, "COPY_MEM") == 0) return COPY_MEM;
    if (strcmp(operacion, "MUTEX_CREATE") == 0) return MUTEX_CREATE;
    if (strcmp(operacion, "SLEEP") == 0) return SLEEP;
    if (strcmp(operacion, "MUTEX_LOCK") == 0) return MUTEX_LOCK;
    if (strcmp(operacion, "MUTEX_UNLOCK") == 0) return MUTEX_UNLOCK;
    if (strcmp(operacion, "MEM_ALLOC") == 0) return MEM_ALLOC;
    if (strcmp(operacion, "MEM_FREE") == 0) return MEM_FREE;
    if (strcmp(operacion, "STDOUT") == 0) return STDOUT; 
    if (strcmp(operacion, "STDIN") == 0) return STDIN;
    if (strcmp(operacion, "INIT_PROC") == 0) return INIT_PROC;
    if (strcmp(operacion, "EXIT") == 0) return EXIT_P;

    return INSTRUCCION_DESCONOCIDA;
}

uint8_t* obtener_registro_8(char* nombre) {
    if(nombre == NULL) return NULL;
    if (strcmp(nombre, "AX") == 0) return &registros_cpu.AX;
    if (strcmp(nombre, "BX") == 0) return &registros_cpu.BX;
    if (strcmp(nombre, "CX") == 0) return &registros_cpu.CX;
    if (strcmp(nombre, "DX") == 0) return &registros_cpu.DX;
    return NULL;
}

uint32_t* obtener_registro_32(char* nombre) {
    if(nombre == NULL) return NULL;
    if (strcmp(nombre, "EAX") == 0) return &registros_cpu.EAX;
    if (strcmp(nombre, "EBX") == 0) return &registros_cpu.EBX;
    if (strcmp(nombre, "ECX") == 0) return &registros_cpu.ECX;
    if (strcmp(nombre, "EDX") == 0) return &registros_cpu.EDX;
    if (strcmp(nombre, "SI") == 0) return &registros_cpu.SI;
    if (strcmp(nombre, "DI") == 0) return &registros_cpu.DI;
    if (strcmp(nombre, "PC") == 0) return &registros_cpu.PC; 
    return NULL;
}


t_instruccion_decodificada decodificar(char* instruccion_cruda) {
    t_instruccion_decodificada inst;
    

    memset(&inst, 0, sizeof(t_instruccion_decodificada));
    instruccion_cruda[strcspn(instruccion_cruda, "\n")] = '\0';

    char** parametros = string_split(instruccion_cruda, " ");
    inst.op_code = traducir_instruccion(parametros[0]);

    switch(inst.op_code) {
        
        case SET: 
            // SET Registro Valor Numérico
            inst.reg_dest_8 = obtener_registro_8(parametros[1]);
            inst.reg_dest_32 = obtener_registro_32(parametros[1]);
            inst.valor_numerico = (uint32_t) atoi(parametros[2]);
            break;

        case SUM:
        case SUB:
            // SUM/SUB Registro Destino Registro Origen
            inst.reg_dest_8 = obtener_registro_8(parametros[1]);
            inst.reg_dest_32 = obtener_registro_32(parametros[1]);
            
            inst.reg_orig_8 = obtener_registro_8(parametros[2]);
            inst.reg_orig_32 = obtener_registro_32(parametros[2]);
            break;

        case JNZ:
            // JNZ Registro Instrucción Numérica
            inst.reg_dest_8 = obtener_registro_8(parametros[1]);
            inst.reg_dest_32 = obtener_registro_32(parametros[1]);
            inst.valor_numerico = (uint32_t) atoi(parametros[2]);
            break;
            
        case MOV_IN:
        case MOV_OUT:
            // MOV Registro Datos
            inst.reg_dest_8 = obtener_registro_8(parametros[1]);
            inst.reg_dest_32 = obtener_registro_32(parametros[1]);
            break;
        case SLEEP:
            // SLEEP Tiempo Numérico
            inst.valor_numerico = (uint32_t) atoi(parametros[1]);
            break;

        case MUTEX_CREATE:
        case MUTEX_LOCK:
        case MUTEX_UNLOCK:
            // MUTEX Nombre del Mutex
            if (parametros[1] != NULL) {
                strcpy(inst.parametro_string, parametros[1]);
            }
            break;
        case MEM_ALLOC:
            inst.valor_numerico = (uint32_t) atoi(parametros[1]);
            inst.size = (uint32_t) atoi(parametros[2]);

            break;
        case MEM_FREE:
            inst.valor_numerico = (uint32_t) atoi(parametros[1]);
            break;
        case COPY_MEM:
            // Recibe (Registro Tamaño)
            inst.reg_dest_8 = obtener_registro_8(parametros[1]);
            inst.reg_dest_32 = obtener_registro_32(parametros[1]);
            break;
            
        case STDIN:
        case STDOUT: 
            // Recibe (Registro Dir_Logica, Registro Tamaño)
            inst.reg_dest_8 = obtener_registro_8(parametros[1]);
            inst.reg_dest_32 = obtener_registro_32(parametros[1]);
            
            inst.reg_orig_8 = obtener_registro_8(parametros[2]);
            inst.reg_orig_32 = obtener_registro_32(parametros[2]);
            break;
            
        case INIT_PROC:
            // Recibe (Archivo, Prioridad)
            if (parametros[1] != NULL) strcpy(inst.parametro_string, parametros[1]);
            inst.valor_numerico = (uint32_t) atoi(parametros[2]); 

            break;
        case NOOP:
        case EXIT_P:
            // No tienen parámetros
            break;

        default:
            break;
    }
    string_array_destroy(parametros);

    return inst;
}

bool ejecutar_instruccion(t_instruccion_decodificada* inst, uint32_t pid) {
    
    
    switch(inst->op_code) {
        
        case NOOP: {
            log_info(logger, "## PID: %d - Ejecutando: NOOP ", pid);
            return false;
        }

        case SET: {
            if (inst->reg_dest_8 != NULL) {
                *(inst->reg_dest_8) = (uint8_t) inst->valor_numerico;
                log_info(logger, "## PID: %d - Ejecutando: SET - %s %d", pid, obtener_nombre_reg_8(inst->reg_dest_8),inst->valor_numerico);

            } else if (inst->reg_dest_32 != NULL) {
                *(inst->reg_dest_32) = inst->valor_numerico;
                log_info(logger, "## PID: %d - Ejecutando: SET - %s %d", pid, obtener_nombre_reg_32(inst->reg_dest_32),inst->valor_numerico);

            } else {
                log_error(logger, "PID: %d - ERROR en SET: Registro inválido.", pid);
                return true; // Error fatal, desalojamos
            }
            return false;
        }

        case SUM: {
            if (inst->reg_dest_8 != NULL && inst->reg_orig_8 != NULL) {

                *(inst->reg_dest_8) += *(inst->reg_orig_8);
                log_info(logger, "## PID: %d - Ejecutando: SUM - %s %d", pid, obtener_nombre_reg_8(inst->reg_dest_8),inst->valor_numerico);

            } else if (inst->reg_dest_32 != NULL && inst->reg_orig_32 != NULL) {
                *(inst->reg_dest_32) += *(inst->reg_orig_32);
                log_info(logger, "## PID: %d - Ejecutando: SUM - %s %d", pid, obtener_nombre_reg_32(inst->reg_dest_32),inst->valor_numerico);

            } else {
                log_error(logger, "PID: %d - ERROR en SUM: Registros inválidos/incompatibles.", pid);
                return true;
            }
            return false;
        }

        case SUB: {
            if (inst->reg_dest_8 != NULL && inst->reg_orig_8 != NULL) {
                *(inst->reg_dest_8) -= *(inst->reg_orig_8);
                log_info(logger, "## PID: %d - Ejecutando: SUB - %s %d", pid, obtener_nombre_reg_8(inst->reg_dest_8),inst->valor_numerico);

            } else if (inst->reg_dest_32 != NULL && inst->reg_orig_32 != NULL) {
                *(inst->reg_dest_32) -= *(inst->reg_orig_32);
                log_info(logger, "## PID: %d - Ejecutando: SUB - %s %d", pid, obtener_nombre_reg_32(inst->reg_dest_32),inst->valor_numerico);

            } else {
                log_error(logger, "PID: %d - ERROR en SUB: Registros inválidos/incompatibles.", pid);
                return true;
            }
            return false;
        }

        case JNZ: {
            bool saltar = false;
            if (inst->reg_dest_8 != NULL && *(inst->reg_dest_8) != 0) saltar = true;
            if (inst->reg_dest_32 != NULL && *(inst->reg_dest_32) != 0) saltar = true;
            if(inst->reg_dest_8 != NULL){
                log_info(logger, "## PID: %d - Ejecutando: JNZ - %s %d", pid, obtener_nombre_reg_8(inst->reg_dest_8),inst->valor_numerico);

            }else {
                log_info(logger, "## PID: %d - Ejecutando: JNZ - %s %d", pid, obtener_nombre_reg_32(inst->reg_dest_32),inst->valor_numerico);

            }

            if (saltar) {
                registros_cpu.PC = inst->valor_numerico; 
                log_info(logger, "PID: %d - JNZ: Salto ejecutado al PC %d", pid, registros_cpu.PC);
            } else {
                log_info(logger, "PID: %d - JNZ: No hubo salto (Registro en 0)", pid);
            }
            return false;
        }

        // --- INSTRUCCIONES DE MEMORIA ---

        case MOV_IN: {
            if (inst->reg_dest_8 != NULL){
                log_info(logger, "## PID: %d - Ejecutando: MOV_IN - %s", pid, obtener_nombre_reg_8(inst->reg_dest_8));
            }else{
                log_info(logger, "## PID: %d - Ejecutando: MOV_IN - %s", pid, obtener_nombre_reg_32(inst->reg_dest_32));
            }
            
            uint32_t bytes_lectura = (inst->reg_dest_32 != NULL) ? 4:1;
            uint32_t dir_fisica = mmu_calculo_fisica(registros_cpu.SI, bytes_lectura,tabla_segmentos, max_segment_size);

            if((int)dir_fisica == -2){
                log_error(logger, "Segmentation Fault en MOV_IN");
                inst->op_code = SEGMENTATION_FAULT;
                return true;
            }
            if((int)dir_fisica == -1){
                log_error(logger, "No en encontro el segmento en MOV_IN");
                inst->op_code = SEGMENTATION_FAULT;
                return true;
            }

            bool fue_error_comunicacion_in;
            void* valor_leido = solicitar_lectura_ms(dir_fisica, bytes_lectura, &fue_error_comunicacion_in);
            if(valor_leido==NULL){
                if (fue_error_comunicacion_in) abortar_por_falla_infraestructura_ms(pid);
                log_error(logger, "Error de comunicacion con el MS en MOV_IN");
                inst->op_code = SEGMENTATION_FAULT;
                return true;
            }
            log_info(logger, "## PID: %d - Accion: LEER - Direccion Fisica: %d - Valor: %u", pid, dir_fisica, valor_para_log(valor_leido, bytes_lectura));

            if(inst->reg_dest_32!=NULL){
                memcpy(inst->reg_dest_32, valor_leido, bytes_lectura);
            }else{
                memcpy(inst->reg_dest_8, valor_leido, bytes_lectura);
            }
            free(valor_leido);
            return false;
        }

        case MOV_OUT: {
            if (inst->reg_dest_8 != NULL){
                log_info(logger, "## PID: %d - Ejecutando: MOV_OUT - %s", pid, obtener_nombre_reg_8(inst->reg_dest_8));
            }else{
                log_info(logger, "## PID: %d - Ejecutando: MOV_OUT - %s", pid,  obtener_nombre_reg_32(inst->reg_dest_32));
            }

            uint32_t bytes_escritura = (inst->reg_dest_32 != NULL) ? 4:1;
            uint32_t dir_fisica = mmu_calculo_fisica(registros_cpu.DI, bytes_escritura,tabla_segmentos,max_segment_size);
            log_info(logger, "calculo la direccion fisica %u", dir_fisica);

            if((int)dir_fisica == -2){
                log_error(logger, "Segmentation Fault en MOV_OUT");
                inst->op_code = SEGMENTATION_FAULT;
                return true;
            }
            if((int)dir_fisica == -1){
                log_error(logger, "No en encontro el segmento en MOV_OUT");
                inst->op_code = SEGMENTATION_FAULT; 
                return true;
            }

            void* valor_enviar = (inst->reg_dest_32 != NULL)? (void*)inst->reg_dest_32 :(void*)inst->reg_dest_8;

            bool fue_error_comunicacion_out;
            bool confirmacion = solicitar_escritura_ms(dir_fisica, bytes_escritura,valor_enviar, &fue_error_comunicacion_out);
            if(!confirmacion){
                if (fue_error_comunicacion_out) abortar_por_falla_infraestructura_ms(pid);
                log_error(logger, "Error de escritura con MS");
                inst->op_code = SEGMENTATION_FAULT;
                return true;
            }
            log_info(logger, "## PID: %d - Accion: ESCRIBIR - Direccion Fisica: %d - Valor: %u", pid, dir_fisica, valor_para_log(valor_enviar, bytes_escritura));


            return false;
        }

        case COPY_MEM: {
            if (inst->reg_dest_8 != NULL){
                log_info(logger, "## PID: %d - Ejecutando: COPY_MEM - %s", pid, obtener_nombre_reg_8(inst->reg_dest_8));
            }else{
                log_info(logger, "## PID: %d - Ejecutando: COPY_MEM - %s", pid,  obtener_nombre_reg_32(inst->reg_dest_32));
            }
            uint32_t tam_copia = (inst->reg_dest_32 != NULL) ? *(inst->reg_dest_32) : *(inst->reg_dest_8);
            uint32_t dir_fisica_origen = mmu_calculo_fisica(registros_cpu.SI, tam_copia,tabla_segmentos,max_segment_size);
            if((int)dir_fisica_origen == -2){
                log_error(logger, "Segmentation Fault en SI del COPY_MEM");
                inst->op_code = SEGMENTATION_FAULT;
                return true;
            }
            if((int)dir_fisica_origen == -1){
                log_error(logger, "No en encontro el segmento en SI del COPY_MEM");
                inst->op_code = SEGMENTATION_FAULT;
                return true;
            }
            uint32_t dir_fisica_destino = mmu_calculo_fisica(registros_cpu.DI, tam_copia,tabla_segmentos,max_segment_size);
            if((int)dir_fisica_destino == -2){
                log_error(logger, "Segmentation Fault en SI del COPY_MEM");
                inst->op_code = SEGMENTATION_FAULT;
                return true;
            }
            if((int)dir_fisica_destino == -1){
                log_error(logger, "No en encontro el segmento en SI del COPY_MEM");
                inst->op_code = SEGMENTATION_FAULT;
                return true;
            }
            bool fue_error_comunicacion;

            log_info(logger, "Iniciando LECTURA desde %u", dir_fisica_origen);
            void* buffer_temporal = solicitar_lectura_ms(dir_fisica_origen,tam_copia, &fue_error_comunicacion);
            if(buffer_temporal==NULL){
                if (fue_error_comunicacion) abortar_por_falla_infraestructura_ms(pid);
                log_error(logger, "Error de comunicacion con el MS en COPY_MEM (lectura)");
                inst->op_code = SEGMENTATION_FAULT;
                return true;
            }

            log_info(logger, "## PID: %d - Accion: LEER - Direccion Fisica: %d - Valor: %u", pid, dir_fisica_origen, valor_para_log(buffer_temporal, tam_copia));

            bool confirmacion = solicitar_escritura_ms(dir_fisica_destino, tam_copia,buffer_temporal, &fue_error_comunicacion);
            if(!confirmacion){
                if (fue_error_comunicacion) abortar_por_falla_infraestructura_ms(pid);
                log_error(logger, "Error de escritura con MS");
                inst->op_code = SEGMENTATION_FAULT;
                return true;
            }
            log_info(logger, "## PID: %d - Accion: ESCRIBIR - Direccion Fisica: %d - Valor: %u", pid, dir_fisica_destino, valor_para_log(buffer_temporal, tam_copia));

            free(buffer_temporal);

            return false;
        }

        //SYSCALLS

        case SLEEP: {
            log_info(logger, "## PID: %d - Ejecutando: SLEEP - %d", pid, inst->valor_numerico);


            return true;
        }

        case MUTEX_CREATE:{
            log_info(logger, "## PID: %d - Ejecutando: MUTEX_CREATE - %s", pid, inst->parametro_string);
            
            return true;
        }
        case MUTEX_LOCK:{
            log_info(logger, "## PID: %d - Ejecutando: MUTEX_LOCK - %s", pid, inst->parametro_string);
            
            return true;
        }
        case MUTEX_UNLOCK: {
            log_info(logger, "## PID: %d - Ejecutando: MUTEX_UNLOCK - %s", pid, inst->parametro_string);
            
            return true;
        }

        case EXIT_P: {
            log_info(logger, "## PID: %d - Ejecutando:EXIT_P", pid);
            
            return true;
        }
        case MEM_ALLOC:{
            log_info(logger, "## PID: %d - Ejecutando: MEM_ALLOC - %d %d", pid, inst->valor_numerico,inst->size);
            return true;
        }
        case MEM_FREE:{
            log_info(logger, "## PID: %d - Ejecutando: MEM_FREE - %d", pid , inst->valor_numerico);
            return true;
        }
        case INIT_PROC:{
            log_info(logger, "## PID: %d - Ejecutando: INIT_PROC - %s %d", pid , inst->parametro_string, inst->valor_numerico);
            return true;
        }
        case STDOUT: {
            // Extraemos los valores de los registros en este instante
            uint32_t dir_logica = (inst->reg_dest_32 != NULL) ? *(inst->reg_dest_32) : *(inst->reg_dest_8);
            uint32_t tamanio = (inst->reg_orig_32 != NULL) ? *(inst->reg_orig_32) : *(inst->reg_orig_8);
            
           
            log_info(logger, "## PID: %d - Ejecutando: STDOUT - %d %d", pid,  dir_logica,tamanio);
            
            inst->valor_numerico = dir_logica; 
            inst->size = tamanio;
            
            return true; // Desalojamos la CPU
        }
        case STDIN: {

            uint32_t dir_logica = (inst->reg_dest_32 != NULL) ? *(inst->reg_dest_32) : *(inst->reg_dest_8);
            uint32_t tamanio = (inst->reg_orig_32 != NULL) ? *(inst->reg_orig_32) : *(inst->reg_orig_8);
            log_info(logger, "## PID: %d - Ejecutando: STDIN - %d %d", pid, dir_logica,tamanio);
            
            
            inst->valor_numerico = dir_logica; 
            inst->size = tamanio;
            
            return true; // Desalojamos la CPU
        }

        default:
            log_error(logger, "PID: %d - INSTRUCCIÓN DESCONOCIDA. Abortando...", pid);
           return true;
    }
}

void* espera_interrupcion(){
    while(1){
        uint32_t pid_int_kernel = recibir_interrupcion();
        if(pid_int_kernel == -1) break;
        
        pthread_mutex_lock(&mutex_interrupcion);
        pid_a_interrumpir = pid_int_kernel;
        pthread_mutex_unlock(&mutex_interrupcion);

    }
    return NULL;
}


void* hilo_escuchar_updates_ms(void* arg){
    uint32_t opcode;

    if(recv(fd_memory_updates, &opcode, sizeof(uint32_t),MSG_WAITALL)<=0){
        log_error(logger, "Se desconecta memory updates");
        return NULL;

    }
    if(opcode==TABLA_INICIAL_MS){
        uint32_t cant_ms;
        recv(fd_memory_updates, &cant_ms, sizeof(uint32_t), MSG_WAITALL);
                
        for (uint32_t i = 0; i < cant_ms; i++) {
            t_memory_stick* ms = malloc(sizeof(t_memory_stick));

            
            recv(fd_memory_updates, &(ms->id), sizeof(uint32_t), MSG_WAITALL);
            recv(fd_memory_updates, &(ms->tamano), sizeof(uint32_t), MSG_WAITALL);
            recv(fd_memory_updates, &(ms->base_global), sizeof(uint32_t), MSG_WAITALL);
            recv(fd_memory_updates, &(ms->limite_global), sizeof(uint32_t), MSG_WAITALL);
            ms->tamano = ms->limite_global - ms->base_global + 1;


            uint32_t len_ip;
            recv(fd_memory_updates, &len_ip, sizeof(uint32_t), MSG_WAITALL);
            ms->ip = malloc(len_ip);
            recv(fd_memory_updates, ms->ip, len_ip, MSG_WAITALL);

            
            uint32_t len_puerto;
            recv(fd_memory_updates, &len_puerto, sizeof(uint32_t), MSG_WAITALL);
            ms->puerto = malloc(len_puerto);
            recv(fd_memory_updates, ms->puerto, len_puerto, MSG_WAITALL);

  
            ms->fd = conectarse_a_modulo(ms->ip, ms->puerto, logger, HANDSHAKE_CPU);
            
            if (ms->fd != -1) {
                send(ms->fd, &cpu_id, sizeof(uint32_t), 0);
                pthread_mutex_lock(&mutex_sticks);
                list_add(tabla_memory_sticks, ms);
                pthread_mutex_unlock(&mutex_sticks);

                log_info(logger, "Conectado al MS %u (Base: %u) en %s:%s", ms->id, ms->base_global, ms->ip, ms->puerto);
            } else {
                log_error(logger, "Falló la conexión directa al MS %u", ms->id);
                free(ms->ip);
                free(ms->puerto);
                free(ms);
            }
        }
    }
    while(1){ //Loop escuchando nuevos o desconectados
        uint32_t cod_op;
        if(recv(fd_memory_updates, &cod_op, sizeof(uint32_t),MSG_WAITALL)<=0){
            log_error(logger, "Updates Memoria desconectado");
            break;
        }
        if(cod_op == NUEVO_MEMORY_STICK){
            log_info(logger, "Nuevo Memory Stick");

            t_memory_stick* ms = malloc(sizeof(t_memory_stick));

            
            recv(fd_memory_updates, &(ms->id), sizeof(uint32_t), MSG_WAITALL);
            recv(fd_memory_updates, &(ms->tamano ), sizeof(uint32_t), MSG_WAITALL);
            recv(fd_memory_updates, &(ms->base_global), sizeof(uint32_t), MSG_WAITALL);
            recv(fd_memory_updates, &(ms->limite_global), sizeof(uint32_t), MSG_WAITALL);
            ms->tamano = ms->limite_global - ms->base_global + 1;


            uint32_t len_ip;
            recv(fd_memory_updates, &len_ip, sizeof(uint32_t), MSG_WAITALL);
            ms->ip = malloc(len_ip);
            recv(fd_memory_updates, ms->ip, len_ip, MSG_WAITALL);

            
            uint32_t len_puerto;
            recv(fd_memory_updates, &len_puerto, sizeof(uint32_t), MSG_WAITALL);
            ms->puerto = malloc(len_puerto);
            recv(fd_memory_updates, ms->puerto, len_puerto, MSG_WAITALL);

  
            ms->fd = conectarse_a_modulo(ms->ip, ms->puerto, logger, HANDSHAKE_CPU);
            
            if (ms->fd != -1) {
                send(ms->fd, &cpu_id, sizeof(uint32_t), 0);
                pthread_mutex_lock(&mutex_sticks);
                list_add(tabla_memory_sticks, ms);
                pthread_mutex_unlock(&mutex_sticks);

                log_info(logger, "Conectado al MS %u (Base: %u) en %s:%s", ms->id, ms->base_global, ms->ip, ms->puerto);
            } else {
                log_error(logger, "Falló la conexión directa al MS %u", ms->id);
                free(ms->ip);
                free(ms->puerto);
                free(ms);
            }
        }
    }
    return NULL;
}

const char* obtener_nombre_reg_8(uint8_t* puntero_reg) {
    if (puntero_reg == NULL) return "N/A";
    if (puntero_reg == &(registros_cpu.AX)) return "AX";
    if (puntero_reg == &(registros_cpu.BX)) return "BX";
    if (puntero_reg == &(registros_cpu.CX)) return "CX";
    if (puntero_reg == &(registros_cpu.DX)) return "DX";
    return "DESCONOCIDO";
}

const char* obtener_nombre_reg_32(uint32_t* puntero_reg) {
    if (puntero_reg == NULL) return "N/A";
    if (puntero_reg == &(registros_cpu.EAX)) return "EAX";
    if (puntero_reg == &(registros_cpu.EBX)) return "EBX";
    if (puntero_reg == &(registros_cpu.ECX)) return "ECX";
    if (puntero_reg == &(registros_cpu.EDX)) return "EDX";
    if (puntero_reg == &(registros_cpu.SI)) return "SI";
    if (puntero_reg == &(registros_cpu.DI)) return "DI";
    return "DESCONOCIDO";
}