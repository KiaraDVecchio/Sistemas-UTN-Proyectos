#include "kernel_memory.h"
#include <pthread.h>
#include <arpa/inet.h>
#include <sys/socket.h>

/* Obtiene la IP real del extremo remoto de un socket ya aceptado.
 * Se usa en el handshake de Memory Stick para no hardcodear la IP,
 * ya que el sistema es distribuido y cada módulo puede correr en
 * una máquina distinta. */
static char* obtener_ip_remota(int fd) {
    struct sockaddr_in addr;
    socklen_t len = sizeof(addr);
    char ip_str[INET_ADDRSTRLEN] = {0};

    if (getpeername(fd, (struct sockaddr*)&addr, &len) == 0) {
        inet_ntop(AF_INET, &(addr.sin_addr), ip_str, INET_ADDRSTRLEN);
        return strdup(ip_str);
    }
    return strdup("0.0.0.0"); /* fallback si falla getpeername */
}


static void destruir_contexto(void* ptr) {
    t_contexto_proceso* ctx = (t_contexto_proceso*)ptr;
    if (ctx == NULL) return;

    if (ctx->tabla_segmentos != NULL) {
        list_destroy_and_destroy_elements(ctx->tabla_segmentos, free);
    }
    if (ctx->instrucciones != NULL) {
        list_destroy_and_destroy_elements(ctx->instrucciones, free);
    }
    free(ctx);
}

static uint32_t traducir_logico_a_fisico(t_contexto_proceso* ctx, uint32_t dir_logica) {
    uint32_t segment_max_size = (uint32_t)config_get_int_value(config, "SEGMENT_MAX_SIZE");
    uint32_t num_seg = dir_logica / segment_max_size;
    uint32_t offset  = dir_logica % segment_max_size;
    for (int i = 0; i < list_size(ctx->tabla_segmentos); i++) {
        t_segmento* seg = list_get(ctx->tabla_segmentos, i);
        if (seg->id == num_seg)
            return seg->base + offset;
    }
    return (uint32_t)-1;
}

/* Saca de lista_memory_sticks el stick conectado por "fd", resta su tamaño de
 * memoria_total_sistema y avisa MEMORIA_CORRUPTA al Kernel. Se llama tanto desde
 * la desconexión detectada por manejar_cliente_memoria como desde cualquier
 * send()/recv() fallido contra el stick (escribir_memoria_fisica, leer_memoria_fisica,
 * ESCRITURA_MEMORIA, LECTURA_MEMORIA), ya que ninguno de esos puntos vuelve a pasar
 * por el loop de manejar_cliente_memoria para ese fd. */
void desconectar_memory_stick(int fd) {
    pthread_mutex_lock(&mutex_lista_ms);

    for(int i = 0; i < list_size(lista_memory_sticks); i++) {
        t_memory_stick* ms = list_get(lista_memory_sticks, i);
        if(ms->fd == fd) {
            memoria_total_sistema -= ms->tamano;

            log_warning(logger, "Memory Stick (ID: %u) desconectado. Tamaño restado: %u bytes", ms->id, ms->tamano);
            log_info(logger, "Memoria total del sistema bajó a: %u bytes", memoria_total_sistema);

            if (fd_kernel != -1 && fd_kernel != 0) {
                t_paquete* paq_corrupta = crear_paquete();
                paq_corrupta->codigo_operacion = MEMORIA_CORRUPTA;
                enviar_paquete(paq_corrupta, fd_kernel);
                eliminar_paquete(paq_corrupta);
                log_error(logger, "## MEMORIA CORRUPTA - Notificando al Kernel Scheduler por desconexión de MS %u", ms->id);
            }

            list_remove(lista_memory_sticks, i);
            pthread_mutex_destroy(&ms->mutex_fd);
            free(ms->ip);
            free(ms->puerto);
            free(ms);
            break;
        }
    }

    pthread_mutex_unlock(&mutex_lista_ms);
}

void manejar_cliente_memoria(void* arg) {
    int cliente_fd = *(int*)arg;
    free(arg);

    while (1) {
        int cod_op = recibir_operacion(cliente_fd);
        if (cod_op == -1) break; // Cliente desconectado

        switch (cod_op) {
            
            case HANDSHAKE_KERNEL:
                fd_kernel = cliente_fd;
                log_info(logger, "## Kernel Scheduler conectado");
                break;

            case HANDSHAKE_SWAP: {

                if (recv(cliente_fd, &tamano_bloque_swap, sizeof(uint32_t), MSG_WAITALL) > 0) {
                    fd_swap = cliente_fd;
                    recv(cliente_fd, &tamano_total_swap, sizeof(uint32_t), MSG_WAITALL);
                    log_info(logger, "## SWAP conectado - Tamaño de Bloque: %u - Total: %u", 
                             tamano_bloque_swap, tamano_total_swap);
                 
                uint32_t cant_bloques = tamano_total_swap / tamano_bloque_swap;
                uint32_t bytes_bitmap = (cant_bloques + 7) / 8; 
                    
                puntero_bitmap = malloc(bytes_bitmap);
                memset(puntero_bitmap, 0, bytes_bitmap); 
                    
                bitmap_swap = bitarray_create_with_mode(puntero_bitmap, bytes_bitmap, LSB_FIRST);
                log_info(logger, "Bitmap de SWAP inicializado con %u bloques.", cant_bloques);
                }
                return;
            }
                  
            case HANDSHAKE_MEMORY_STICK: {
                uint32_t tamano_ms;
                if (recv(cliente_fd, &tamano_ms, sizeof(uint32_t), MSG_WAITALL) > 0) {
                    // Recibir el puerto
                    uint32_t len_puerto;
                    recv(cliente_fd, &len_puerto, sizeof(uint32_t), MSG_WAITALL);
                    char* puerto_ms = malloc(len_puerto);
                    recv(cliente_fd, puerto_ms, len_puerto, MSG_WAITALL);

                    t_memory_stick* nuevo_ms = malloc(sizeof(t_memory_stick));
                    nuevo_ms->fd = cliente_fd;
                    nuevo_ms->tamano = tamano_ms;
                    nuevo_ms->ip = obtener_ip_remota(cliente_fd);
                    nuevo_ms->puerto = puerto_ms;
                    pthread_mutex_init(&nuevo_ms->mutex_fd, NULL);

                    // ---> BLOQUEAMOS: Vamos a modificar la lista y la memoria total <---
                    pthread_mutex_lock(&mutex_lista_ms);
                    
                    // Calcular límites basados en el estado ACTUAL seguro
                    uint32_t base = memoria_total_sistema;
                    uint32_t limite = base + tamano_ms - 1;
                    
                    nuevo_ms->id = list_size(lista_memory_sticks) + 1;
                    nuevo_ms->base_global = base;
                    nuevo_ms->limite_global = limite;

                    list_add(lista_memory_sticks, nuevo_ms);
                    memoria_total_sistema += tamano_ms; // Actualizo el total de memoria

                    // ---> LIBERAMOS: Ya guardamos todo de forma segura <---
                    pthread_mutex_unlock(&mutex_lista_ms);

                    // Agregamos el rango recién sumado como hueco libre para que
                    // MEM_ALLOC pueda usarlo sin depender de una compactación previa.
                    pthread_mutex_lock(&mutex_huecos);
                    t_hueco* hueco_nuevo_ms = malloc(sizeof(t_hueco));
                    hueco_nuevo_ms->base   = base;
                    hueco_nuevo_ms->limite = limite;
                    hueco_nuevo_ms->tamano = tamano_ms;
                    list_add(huecos_libres, hueco_nuevo_ms);
                    pthread_mutex_unlock(&mutex_huecos);

                    // Logs (Ya fuera del mutex para no bloquear mientras imprimimos)
                    log_info(logger, "## Memory Stick de %u bytes Conectada", tamano_ms);
                    log_info(logger, "Nuevo MS (ID: %u) cubre el rango físico: %u a %u", nuevo_ms->id, base, limite);
                    log_info(logger, "Memoria total del sistema actualizada a: %u bytes", memoria_total_sistema);
                    
                    // ---> NOTIFICAR AL SCHEDULER QUE SE AMPLIÓ LA MEMORIA <---
                    if (fd_kernel != -1 && fd_kernel != 0) {
                        t_paquete* paq_ampliacion = crear_paquete();
                        paq_ampliacion->codigo_operacion = MEMORIA_AMPLIADA;
                        enviar_paquete(paq_ampliacion, fd_kernel);
                        eliminar_paquete(paq_ampliacion);
                        log_info(logger, "Notificación de AMPLIACIÓN de memoria enviada al Scheduler");
                    }

                    // --- AVISO EN VIVO A TODAS LAS CPUs POR SUS SOCKETS DE UPDATES ---
                    pthread_mutex_lock(&mutex_cpu_updates);
                    for (int i = 0; i < list_size(lista_fd_cpu_updates); /* incremento manual */) {
                        int* fd_cpu = list_get(lista_fd_cpu_updates, i);

                        t_paquete* paq_actualizacion = crear_paquete();
                        paq_actualizacion->codigo_operacion = NUEVO_MEMORY_STICK;

                        agregar_a_paquete(paq_actualizacion, &(nuevo_ms->id), sizeof(uint32_t));
                        agregar_a_paquete(paq_actualizacion, &(nuevo_ms->tamano),sizeof(uint32_t));
                        agregar_a_paquete(paq_actualizacion, &(nuevo_ms->base_global), sizeof(uint32_t));
                        agregar_a_paquete(paq_actualizacion, &(nuevo_ms->limite_global), sizeof(uint32_t));

                        uint32_t len_ip = strlen(nuevo_ms->ip) + 1;
                        agregar_a_paquete(paq_actualizacion, &len_ip, sizeof(uint32_t));
                        agregar_a_paquete(paq_actualizacion, nuevo_ms->ip, len_ip);

                        uint32_t len_pto_str = strlen(nuevo_ms->puerto) + 1;
                        agregar_a_paquete(paq_actualizacion, &len_pto_str, sizeof(uint32_t));
                        agregar_a_paquete(paq_actualizacion, nuevo_ms->puerto, len_pto_str);

                        /* Probamos si el socket sigue vivo antes de mandar el paquete real:
                         * si la CPU ya se desconectó, la sacamos de la lista sin crashear. */
                        int probe = send(*fd_cpu, NULL, 0, MSG_NOSIGNAL);
                        if (probe == -1) {
                            log_warning(logger, "CPU con FD %d desconectada, removiendo de updates", *fd_cpu);
                            list_remove(lista_fd_cpu_updates, i);
                            free(fd_cpu);
                        } else {
                            enviar_paquete(paq_actualizacion, *fd_cpu);
                            i++;
                        }

                        eliminar_paquete(paq_actualizacion);
                    }
                    pthread_mutex_unlock(&mutex_cpu_updates);
                    log_info(logger, "Notificación push de nuevo MS (ID: %u) enviada a las CPUs conectadas", nuevo_ms->id);
                }
                /* A partir de aca el fd del memory stick solo recibe respuestas a pedidos
                 * puntuales (ESCRITURA_MEMORIA/LECTURA_MEMORIA), leidas directamente por
                 * escribir_memoria_fisica/leer_memoria_fisica. Si este thread genérico
                 * siguiera hasta el proximo recibir_operacion() sobre el mismo fd, competiria
                 * por esos bytes de respuesta y los interpretaria como una operación desconocida. */
                return;
            }
            case HANDSHAKE_CPU: {
                uint32_t cpu_id;
                if (recv(cliente_fd, &cpu_id, sizeof(uint32_t), MSG_WAITALL) > 0) {
                    log_info(logger, "## CPU %u Conectada", cpu_id);

                    // Enviar SEGMENT_MAX_SIZE
                    uint32_t max_tam_segmento = (uint32_t)config_get_int_value(config, "SEGMENT_MAX_SIZE");
                    send(cliente_fd, &max_tam_segmento, sizeof(uint32_t), 0);
                }
            break;
            }
            case HANDSHAKE_CPU_UPDATES: {
                /* Cada CPU abre su propio socket dedicado a updates: lo agregamos a la
                 * lista en vez de sobreescribir una única variable global, para que
                 * TODAS las CPUs conectadas reciban las notificaciones de Memory Stick. */
                int* fd_guardado = malloc(sizeof(int));
                *fd_guardado = cliente_fd;

                pthread_mutex_lock(&mutex_cpu_updates);
                list_add(lista_fd_cpu_updates, fd_guardado);
                pthread_mutex_unlock(&mutex_cpu_updates);

                log_info(logger, "## Socket dedicado para actualizaciones de CPU conectado (FD: %d)", cliente_fd);

                uint32_t opcode = TABLA_INICIAL_MS;
                send(cliente_fd, &opcode, sizeof(uint32_t), 0);

                //  MUTEX ANTES DE TOCAR LA LISTA <---
                pthread_mutex_lock(&mutex_lista_ms);

                uint32_t cant_ms = list_size(lista_memory_sticks);
                send(cliente_fd, &cant_ms, sizeof(uint32_t), 0);

                for(int i = 0; i < cant_ms; i++) {
                    t_memory_stick* ms = list_get(lista_memory_sticks, i);

                    send(cliente_fd, &(ms->id), sizeof(uint32_t), 0);
                    send(cliente_fd,&(ms->tamano),sizeof(uint32_t),0);
                    send(cliente_fd, &(ms->base_global), sizeof(uint32_t), 0);
                    send(cliente_fd, &(ms->limite_global), sizeof(uint32_t), 0);

                    uint32_t len_ip = strlen(ms->ip) + 1;
                    send(cliente_fd, &len_ip, sizeof(uint32_t), 0);
                    send(cliente_fd, ms->ip, len_ip, 0);

                    uint32_t len_puerto = strlen(ms->puerto) + 1;
                    send(cliente_fd, &len_puerto, sizeof(uint32_t), 0);
                    send(cliente_fd, ms->puerto, len_puerto, 0);
                }

                // ---> LIBERO EL MUTEX <---
                pthread_mutex_unlock(&mutex_lista_ms);

                log_info(logger, "Guía de %u Memory Sticks enviada a la CPU", cant_ms);
                break;
            }
            
            case SOLICITUD_INSTRUCCION: {
                t_list* pedido = recibir_paquete(cliente_fd);
                uint32_t pid_inst = *(uint32_t*)list_get(pedido, 0);
                uint32_t pc_inst = *(uint32_t*)list_get(pedido, 1);

                // Retraso de configuración
                usleep(config_get_int_value(config, "INSTRUCTION_DELAY") * 1000);

                char* pid_key = string_itoa(pid_inst);

                pthread_mutex_lock(&mutex_procesos); // <-- BLOQUEAR
                bool existe = dictionary_has_key(procesos, pid_key);
                pthread_mutex_unlock(&mutex_procesos); // <-- LIBERAR
                
                if (!existe) {
                    log_warning(logger, "Intento de acceso a PID %u inexistente", pid_inst);
                } else {
                    log_info(logger, "## Accediendo a estructuras de gestión del PID: %u", pid_inst);
                }
                free(pid_key);
                
                char* instruccion = obtener_linea_de_archivo(pid_inst, pc_inst);
    
                t_paquete* resp = crear_paquete();
                resp->codigo_operacion = RESPUESTA_INSTRUCCION;
    
                if (instruccion) {
                    agregar_a_paquete(resp, instruccion, strlen(instruccion) + 1);
                    log_info(logger, "## PID: %u - Obtener instrucción: %u - Instrucción: %s", pid_inst, pc_inst, instruccion);
                    free(instruccion);
                } else {
                    char* error = "EXIT"; // Si no hay más líneas, termina
                    agregar_a_paquete(resp, error, strlen(error) + 1);
                }

                enviar_paquete(resp, cliente_fd);
                list_destroy_and_destroy_elements(pedido, free);
                eliminar_paquete(resp);
                break;
            }

            case CREAR_PROCESO: {
                t_list* datos_p    = recibir_paquete(cliente_fd);
                uint32_t pid_nuevo = *(uint32_t*)list_get(datos_p, 0);
                char*    path_recibido = (char*)list_get(datos_p, 1);

                t_contexto_proceso* contexto = malloc(sizeof(t_contexto_proceso));
                contexto->pid             = pid_nuevo;
                memset(&(contexto->registros), 0, sizeof(t_registros));
                contexto->tabla_segmentos = list_create();
                contexto->instrucciones   = NULL;

                /* Preload del archivo de instrucciones — una sola lectura de disco */
                contexto->instrucciones = cargar_instrucciones_desde_archivo(pid_nuevo, path_recibido);
                if (contexto->instrucciones == NULL) {
                    log_error(logger, "PID %u: no se pudo cargar el archivo de instrucciones — proceso no creado", pid_nuevo);
                    list_destroy(contexto->tabla_segmentos);
                    free(contexto);
                    list_destroy_and_destroy_elements(datos_p, free);
                    uint32_t error = RESPUESTA_ERROR;
                    send(cliente_fd, &error, sizeof(uint32_t), 0);
                    break;
                }

                char* pid_key = string_itoa(pid_nuevo);
                pthread_mutex_lock(&mutex_procesos);
                dictionary_put(procesos, pid_key, contexto);
                pthread_mutex_unlock(&mutex_procesos);

                log_info(logger, "## PID: %u - Proceso Creado", pid_nuevo);

                free(pid_key);
                list_destroy_and_destroy_elements(datos_p, free);

                uint32_t respuesta = RESPUESTA_OK;
                send(cliente_fd, &respuesta, sizeof(uint32_t), 0);
                break;
            }

            // SEGUN LO QUE HABLAMOS CON DAMIAN, CPU HACE LA COMUNICACION DIRECTA
            // LECTURA Y ESCRITURA QUEDAN PARA CUANDO KERNEL SCHEDULER QUIERA COMUNICARSE
            // POR EJEMPLO IO_STDIN O IO_STDOUT
            case ESCRITURA_MEMORIA: {
                t_list* datos_escritura = recibir_paquete(cliente_fd);
                uint32_t pid = *(uint32_t*)list_get(datos_escritura, 0);
                uint32_t dir_logica = *(uint32_t*)list_get(datos_escritura, 1);
                uint32_t tamanio = *(uint32_t*)list_get(datos_escritura, 2);
                void* datos = list_get(datos_escritura, 3);

                char* pid_key_e = string_itoa(pid);
                pthread_mutex_lock(&mutex_procesos);
                t_contexto_proceso* ctx_e = dictionary_get(procesos, pid_key_e);
                uint32_t dir_fisica_e = (ctx_e != NULL) ? traducir_logico_a_fisico(ctx_e, dir_logica) : (uint32_t)-1;
                pthread_mutex_unlock(&mutex_procesos);
                free(pid_key_e);

                log_info(logger, "PID: %u - Acción: ESCRIBIR - Dirección lógica: %u - Dirección física: %u - Tamaño: %u", pid, dir_logica, dir_fisica_e, tamanio);

                if (dir_fisica_e == (uint32_t)-1) {
                    log_error(logger, "Fallo de Segmentación: dirección lógica %u inválida para PID %u", dir_logica, pid);
                    uint32_t error_op = SEGMENTATION_FAULT;
                    send(cliente_fd, &error_op, sizeof(uint32_t), 0);
                    list_destroy_and_destroy_elements(datos_escritura, free);
                    break;
                }

                // Usamos escribir_memoria_fisica (la misma que usa compactación/SWAP) en vez de
                // reenviar el paquete a un unico memory stick, porque un segmento puede estar
                // partido entre dos sticks contiguos: mandarle el tamaño completo a uno solo
                // hacia que ese stick escribiera fuera de su propio espacio reservado.
                bool escritura_ok = escribir_memoria_fisica(dir_fisica_e, tamanio, datos);

                uint32_t resp_op = escritura_ok ? RESPUESTA_OK : SEGMENTATION_FAULT;
                send(cliente_fd, &resp_op, sizeof(uint32_t), 0);

                list_destroy_and_destroy_elements(datos_escritura, free);
                break;
            }

            case LECTURA_MEMORIA: {
                t_list* datos_lectura = recibir_paquete(cliente_fd);
                uint32_t pid = *(uint32_t*)list_get(datos_lectura, 0);
                uint32_t dir_logica = *(uint32_t*)list_get(datos_lectura, 1);
                uint32_t tamanio = *(uint32_t*)list_get(datos_lectura, 2);

                char* pid_key_l = string_itoa(pid);
                pthread_mutex_lock(&mutex_procesos);
                t_contexto_proceso* ctx_l = dictionary_get(procesos, pid_key_l);
                uint32_t dir_fisica_l = (ctx_l != NULL) ? traducir_logico_a_fisico(ctx_l, dir_logica) : (uint32_t)-1;
                pthread_mutex_unlock(&mutex_procesos);
                free(pid_key_l);

                log_info(logger, "PID: %u - Acción: LEER - Dirección lógica: %u - Dirección física: %u - Tamaño: %u", pid, dir_logica, dir_fisica_l, tamanio);

                if (dir_fisica_l == (uint32_t)-1) {
                    log_error(logger, "Fallo de Segmentación: dirección lógica %u inválida para PID %u", dir_logica, pid);
                    uint32_t error_op = SEGMENTATION_FAULT;
                    send(cliente_fd, &error_op, sizeof(uint32_t), 0);
                    list_destroy_and_destroy_elements(datos_lectura, free);
                    break;
                }

                // Igual que en ESCRITURA_MEMORIA: usamos leer_memoria_fisica para que
                // fragmente correctamente si el rango pedido cruza mas de un memory stick.
                void* datos_leidos = leer_memoria_fisica(dir_fisica_l, tamanio);

                if (datos_leidos != NULL) {
                    t_paquete* paq_cpu = crear_paquete();
                    paq_cpu->codigo_operacion = DATOS_LECTURA;
                    agregar_a_paquete(paq_cpu, datos_leidos, tamanio);
                    enviar_paquete(paq_cpu, cliente_fd);
                    eliminar_paquete(paq_cpu);
                    free(datos_leidos);
                } else {
                    log_error(logger, "Fallo de Segmentación: no se pudo leer la dirección física %u.", dir_fisica_l);
                    uint32_t error_op = SEGMENTATION_FAULT;
                    send(cliente_fd, &error_op, sizeof(uint32_t), 0);
                }

                list_destroy_and_destroy_elements(datos_lectura, free);
                break;
            }
            
            case FINALIZAR_PROCESO: {
                t_list* datos_f = recibir_paquete(cliente_fd);
                uint32_t pid_a_borrar = *(uint32_t*)list_get(datos_f, 0);

                char* key = string_itoa(pid_a_borrar);
                
                // Bloqueamos con mutex para que sea seguro
                pthread_mutex_lock(&mutex_procesos);
                if (dictionary_has_key(procesos, key)) {
                    t_contexto_proceso* ctx_a_borrar = dictionary_get(procesos, key);
                    /* Antes de destruir el contexto, liberar huecos físicos o
                     * bloques de SWAP asociados a cada uno de sus segmentos */
                    liberar_huecos_de_proceso(ctx_a_borrar);

                    dictionary_remove_and_destroy(procesos, key, destruir_contexto);
                    log_info(logger, "## PID: %u - Proceso Finalizado (Removido de Memoria)", pid_a_borrar);
                }
                pthread_mutex_unlock(&mutex_procesos);

                free(key);
                list_destroy_and_destroy_elements(datos_f, free);
                break;
            }
            
            case SOLICITUD_CONTEXTO: 
                    {
                        uint32_t pid_a_buscar;
                        recv(cliente_fd, &pid_a_buscar, sizeof(uint32_t), MSG_WAITALL);
                        
                        log_info(logger, "## Contexto Solicitado - PID: %u", pid_a_buscar);
                        char* pid_key = string_itoa(pid_a_buscar);
                        
                        // Buscamos el contexto que creamos en CREAR_PROCESO
                        pthread_mutex_lock(&mutex_procesos);
                        t_contexto_proceso* contexto_guardado = dictionary_get(procesos, pid_key);
                        pthread_mutex_unlock(&mutex_procesos);

                        if (contexto_guardado != NULL) {
                        enviar_contexto(cliente_fd, CONTEXTO_RESPUESTA, contexto_guardado);                        
                        log_info(logger, "Contexto del PID %u enviado a la CPU.", pid_a_buscar);
                        } else {
                        log_error(logger, "Error: No se encontró el contexto para el PID %u", pid_a_buscar);
                    }
    
                    free(pid_key);
                    break;
                    }
            case ACTUALIZAR_CONTEXTO:
                    {
                        t_contexto_proceso* ctx_recibido = recibir_contexto(cliente_fd);
                        uint32_t pid_actualizado = ctx_recibido->pid;

                        log_info(logger, "## Contexto Actualizado - PID: %u - PC: %u", 
                                pid_actualizado, ctx_recibido->registros.PC);

                        char* pid_key = string_itoa(pid_actualizado);

                        pthread_mutex_lock(&mutex_procesos);
                        t_contexto_proceso* contexto_guardado = dictionary_get(procesos, pid_key);
                        if (contexto_guardado != NULL) {
                            contexto_guardado->registros = ctx_recibido->registros; // Solo actualizamos registros
                        }
                        pthread_mutex_unlock(&mutex_procesos);

                        list_destroy_and_destroy_elements(ctx_recibido->tabla_segmentos, free);
                        free(ctx_recibido);
                        
                        free(pid_key);

                        // Le mandamos el OK a la CPU
                        uint32_t ok = RESPUESTA_OK;
                        send(cliente_fd, &ok, sizeof(uint32_t), 0);
                        
                        break;
                    }

            case SUSPENDER_PROCESO: {
                uint32_t pid_a_suspender;
                recv(cliente_fd, &pid_a_suspender, sizeof(uint32_t), MSG_WAITALL);

                char* pid_key = string_itoa(pid_a_suspender);
                
                pthread_mutex_lock(&mutex_procesos);
                t_contexto_proceso* ctx = dictionary_get(procesos, pid_key);
                pthread_mutex_unlock(&mutex_procesos);

                if (ctx != NULL) {
                    log_info(logger, "## Suspendiendo PID: %u", pid_a_suspender);

                    bool todo_ok = true;
                    for(int i = 0; i < list_size(ctx->tabla_segmentos); i++) {
                        t_segmento* seg = list_get(ctx->tabla_segmentos, i);
                        if (!suspender_segmento(pid_a_suspender, seg, fd_swap)) {
                            todo_ok = false;
                        }
                    }

                    // Avisar al Kernel que terminó
                    uint32_t resp_op = todo_ok ? SUSPENSION_OK : RESPUESTA_ERROR;
                    send(cliente_fd, &resp_op, sizeof(uint32_t), 0);
                    if (todo_ok) {
                        log_info(logger, "## PID: %u suspendido exitosamente", pid_a_suspender);
                    } else {
                        log_error(logger, "PID %u: fallo al suspender uno o mas segmentos", pid_a_suspender);
                    }
                } else {
                    log_error(logger, "No se encontró el PID %u para suspender", pid_a_suspender);
                    // sin esto el Kernel Scheduler queda colgado para siempre en el recv
                    // de la respuesta a este pedido
                    uint32_t error_op = RESPUESTA_ERROR;
                    send(cliente_fd, &error_op, sizeof(uint32_t), 0);
                }
                free(pid_key);
                break;
            }
            case DESUSPENDER_PROCESO: {
                uint32_t pid_a_desuspender;
                recv(cliente_fd, &pid_a_desuspender, sizeof(uint32_t), MSG_WAITALL);

                char* pid_key = string_itoa(pid_a_desuspender);

                pthread_mutex_lock(&mutex_procesos);
                t_contexto_proceso* ctx = dictionary_get(procesos, pid_key);
                pthread_mutex_unlock(&mutex_procesos);

                free(pid_key);

                if (ctx == NULL) {
                    log_error(logger, "PID %u no encontrado para des-suspender", pid_a_desuspender);
                    uint32_t err = RESPUESTA_ERROR;
                    send(cliente_fd, &err, sizeof(uint32_t), 0);
                    break;
                }


                /* Se mantiene mutex_huecos tomado desde la verificación hasta reservar el
                 * espacio de todos los segmentos, para evitar que otra operación concurrente
                 * (p.ej. un MEM_ALLOC) se quede con los huecos ya "verificados" en el medio. */
                pthread_mutex_lock(&mutex_huecos);
                bool hay_espacio = verificar_espacio_para_desuspension(ctx->tabla_segmentos);

                if (!hay_espacio) {
                    pthread_mutex_unlock(&mutex_huecos);
                    log_warning(logger,
                        "PID %u: sin huecos suficientes para des-suspender todos los segmentos",
                        pid_a_desuspender);
                    uint32_t sin_espacio = SIN_ESPACIO_DESUSPENSION;
                    send(cliente_fd, &sin_espacio, sizeof(uint32_t), 0);
                    break;
                }

                int segmentos_reservados = 0;
                bool reserva_ok = true;

                for (int i = 0; i < list_size(ctx->tabla_segmentos); i++) {
                    t_segmento* seg = list_get(ctx->tabla_segmentos, i);
                    uint32_t tamano_seg = seg->limite - seg->base + 1;

                    if (!reservar_hueco_para_segmento(seg, tamano_seg)) {
                        reserva_ok = false;
                        break;
                    }
                    segmentos_reservados++;
                }

                if (!reserva_ok) {
                    /* No debería suceder tras verificar_espacio_para_desuspension, pero por las
                     * dudas se deshacen las reservas ya hechas antes de soltar el lock. */
                    for (int i = 0; i < segmentos_reservados; i++) {
                        t_segmento* seg = list_get(ctx->tabla_segmentos, i);
                        uint32_t tamano_seg = seg->limite - seg->base + 1;
                        liberar_hueco_reservado(seg, tamano_seg);
                    }
                    pthread_mutex_unlock(&mutex_huecos);

                    log_warning(logger,
                        "PID %u: no se pudo reservar espacio para des-suspender todos los segmentos",
                        pid_a_desuspender);
                    uint32_t sin_espacio = SIN_ESPACIO_DESUSPENSION;
                    send(cliente_fd, &sin_espacio, sizeof(uint32_t), 0);
                    break;
                }

                pthread_mutex_unlock(&mutex_huecos);

                log_info(logger, "## Des-suspendiendo PID: %u", pid_a_desuspender);

                for (int i = 0; i < list_size(ctx->tabla_segmentos); i++) {
                    t_segmento* seg = list_get(ctx->tabla_segmentos, i);
                    desuspender_segmento(pid_a_desuspender, seg, fd_swap);
                }

                log_info(logger, "## PID: %u des-suspendido exitosamente", pid_a_desuspender);
                uint32_t ok = DESUSPENSION_OK;
                send(cliente_fd, &ok, sizeof(uint32_t), 0);
                break;
            }
            case MEM_ALLOC: {
                t_list* datos_alloc = recibir_paquete(cliente_fd);
                uint32_t pid             = *(uint32_t*)list_get(datos_alloc, 0);
                uint32_t id_segmento     = *(uint32_t*)list_get(datos_alloc, 1);
                uint32_t tamano_segmento = *(uint32_t*)list_get(datos_alloc, 2);
                // es_reintento=1 significa que el Scheduler ya compactó por este mismo
                // pedido; si acá tampoco entra, no tiene sentido pedir otra compactación.
                uint32_t es_reintento = (list_size(datos_alloc) > 3) ? *(uint32_t*)list_get(datos_alloc, 3) : 0;

                log_info(logger, "Solicitud de MEM_ALLOC - PID: %u - Seg: %u - Tamaño: %u", pid, id_segmento, tamano_segmento);

                pthread_mutex_lock(&mutex_procesos);
                pthread_mutex_lock(&mutex_huecos);

                // 1. Buscamos el contexto del proceso
                char* pid_key = string_itoa(pid);
                t_contexto_proceso* ctx = dictionary_get(procesos, pid_key);
                free(pid_key);

                if (ctx != NULL) {
                    // 2. Buscamos un hueco libre usando el Algoritmo (Lo haremos en el próximo paso)
                    t_hueco* hueco_elegido = buscar_hueco_libre(tamano_segmento); 

                    if (hueco_elegido != NULL) {
                        // 3. Hay espacio: Creamos el segmento y actualizamos el hueco
                        t_segmento* nuevo_segmento = malloc(sizeof(t_segmento));
                        nuevo_segmento->id = id_segmento; // ID enviado por la CPU, no list_size
                        nuevo_segmento->base = hueco_elegido->base;
                        nuevo_segmento->limite = hueco_elegido->base + tamano_segmento - 1;
                        
                        list_add(ctx->tabla_segmentos, nuevo_segmento);
                        
                        hueco_elegido->base += tamano_segmento;
                        hueco_elegido->tamano -= tamano_segmento;
                        
                        if (hueco_elegido->tamano == 0) {
                            for(int i = 0; i < list_size(huecos_libres); i++) {
                                if(list_get(huecos_libres, i) == hueco_elegido) {
                                    list_remove(huecos_libres, i);
                                    free(hueco_elegido); // Liberamos la memoria del struct
                                    break;
                                }
                            }
                        }

                        log_info(logger, "## PID: %u - Crear Segmento - Id: %u - Tamaño: %u", pid, nuevo_segmento->id, tamano_segmento);
                        
                        uint32_t ok = RESPUESTA_OK;
                        send(cliente_fd, &ok, sizeof(uint32_t), 0);
                    } else if (!es_reintento) {
                        /* Sin hueco contiguo suficiente: notificar al Scheduler para que
                         * desaloje todas las CPUs y luego envíe EJECUTAR_COMPACTACION.
                         * No se llama a ninguna función de recv() desde este hilo. */
                        log_warning(logger, "No hay espacio contiguo para %u bytes. Solicitando compactación al Scheduler.", tamano_segmento);
                        uint32_t solicitud = SOLICITUD_COMPACTACION;
                        send(fd_kernel, &solicitud, sizeof(uint32_t), 0);
                    } else {
                        // Ya se compactó por este pedido y sigue sin entrar: no tiene sentido
                        // pedir otra compactación, se le avisa al Scheduler que se bloquee.
                        log_warning(logger, "PID %u: sigue sin haber espacio para %u bytes ni siquiera compactando.", pid, tamano_segmento);
                        uint32_t sin_espacio = SIN_ESPACIO_MEMORIA;
                        send(fd_kernel, &sin_espacio, sizeof(uint32_t), 0);
                    }
                } else {
                    log_error(logger, "MEM_ALLOC falló: PID %u no encontrado", pid);
                    // sin esto el Kernel Scheduler queda colgado para siempre en el recv
                    // de la respuesta a este pedido
                    uint32_t error_op = RESPUESTA_ERROR;
                    send(cliente_fd, &error_op, sizeof(uint32_t), 0);
                }

                pthread_mutex_unlock(&mutex_huecos);
                pthread_mutex_unlock(&mutex_procesos);
                list_destroy_and_destroy_elements(datos_alloc, free);
                break;
            }
            case MEM_FREE: {
                t_list* datos_free = recibir_paquete(cliente_fd);
                uint32_t pid = *(uint32_t*)list_get(datos_free, 0);
                uint32_t id_segmento = *(uint32_t*)list_get(datos_free, 1);

                pthread_mutex_lock(&mutex_procesos);
                pthread_mutex_lock(&mutex_huecos);

                char* pid_key = string_itoa(pid);
                t_contexto_proceso* ctx = dictionary_get(procesos, pid_key);
                free(pid_key);

                if (ctx != NULL) {
                    bool encontrado = false;
                    for (int i = 0; i < list_size(ctx->tabla_segmentos); i++) {
                        t_segmento* seg = list_get(ctx->tabla_segmentos, i);
                        
                        if (seg->id == id_segmento) {
                            uint32_t tamano_liberado = seg->limite - seg->base + 1;
                            
                            log_info(logger, "## PID: %u - Eliminar Segmento - Id Segmento: %u - Tamaño: %u", pid, id_segmento, tamano_liberado);
                            
                            // 1. Devolver el espacio a la lista de huecos libres
                            t_hueco* nuevo_hueco = malloc(sizeof(t_hueco));
                            nuevo_hueco->base = seg->base;
                            nuevo_hueco->limite = seg->limite;
                            nuevo_hueco->tamano = tamano_liberado;
                            list_add(huecos_libres, nuevo_hueco);

                            // 2. Consolidar huecos linderos pegados
                            consolidar_huecos();
                            
                            // 3. Liberar bloques en SWAP (si el segmento estaba suspendido)
                            liberar_bloques_swap(pid, id_segmento);

                            // 4. Borrar de la tabla del proceso
                            list_remove(ctx->tabla_segmentos, i);
                            free(seg);
                            encontrado = true;
                            
                            uint32_t ok = RESPUESTA_OK;
                            send(cliente_fd, &ok, sizeof(uint32_t), 0);
                            break;
                        }
                    }
                    if (!encontrado) {
                        log_error(logger, "Segmento %u no existe en PID %u", id_segmento, pid);
                        uint32_t error_op = RESPUESTA_ERROR;
                        send(cliente_fd, &error_op, sizeof(uint32_t), 0);
                    }
                } else {
                    log_error(logger, "PID %u no encontrado", pid);
                    // sin esto el Kernel Scheduler queda colgado para siempre en el recv
                    // de la respuesta a este pedido
                    uint32_t error_op = RESPUESTA_ERROR;
                    send(cliente_fd, &error_op, sizeof(uint32_t), 0);
                }

                pthread_mutex_unlock(&mutex_huecos);
                pthread_mutex_unlock(&mutex_procesos);

                list_destroy_and_destroy_elements(datos_free, free);
                break;
            }            
        
            case EJECUTAR_COMPACTACION: {
                
                log_info(logger, "## Inicio de compactación");

                ejecutar_algoritmo_compactacion();

                int compaction_delay = config_get_int_value(config, "COMPACTION_DELAY");
                if (compaction_delay > 0) {
                    usleep((useconds_t)compaction_delay * 1000);
                }

                log_info(logger, "## Fin de compactación");

                uint32_t fin = FIN_COMPACTACION;
                send(cliente_fd, &fin, sizeof(uint32_t), 0);
                break;
            }

            default:
            log_warning(logger, "Operación desconocida: %d", cod_op);
            break;
        }
    }

    desconectar_memory_stick(cliente_fd);

    // Si este FD también estaba registrado como socket de updates de alguna CPU,
    // lo removemos para no volver a intentar escribirle.
    pthread_mutex_lock(&mutex_cpu_updates);
    for (int i = 0; i < list_size(lista_fd_cpu_updates); i++) {
        int* fd_guardado = list_get(lista_fd_cpu_updates, i);
        if (*fd_guardado == cliente_fd) {
            list_remove(lista_fd_cpu_updates, i);
            free(fd_guardado);
            break;
        }
    }
    pthread_mutex_unlock(&mutex_cpu_updates);

    close(cliente_fd);
}

void atender_clientes_memoria(int server_fd) {
    
    while (1) {
        int* cliente_fd = malloc(sizeof(int));
        *cliente_fd = esperar_cliente(server_fd);
        
        pthread_t hilo;
        if(pthread_create(&hilo, NULL, (void*)manejar_cliente_memoria, cliente_fd) != 0) {
            log_error(logger, "Error creando hilo para cliente");
            free(cliente_fd);
        } else {
            pthread_detach(hilo);
        }
    }
}