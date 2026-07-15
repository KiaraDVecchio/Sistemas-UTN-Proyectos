#include <kernel_memory.h>


t_list* cargar_instrucciones_desde_archivo(uint32_t pid, char* path_recibido) {
    char* path;
    if (path_recibido[0] == '/') {
        /* Path absoluto: se usa tal cual, sin prefijar SCRIPTS_BASEPATH */
        path = strdup(path_recibido);
    } else {
        char* base = config_get_string_value(config, "SCRIPTS_BASEPATH");
        path = string_from_format("%s/%s", base, path_recibido);
    }

    FILE* archivo = fopen(path, "r");
    if (!archivo) {
        log_error(logger, "PID %u: no se pudo abrir el archivo %s", pid, path);
        free(path);
        return NULL;
    }
    free(path);

    t_list* lista  = list_create();
    char*   linea  = NULL;
    size_t  len    = 0;

    while (getline(&linea, &len, archivo) != -1) {
        linea[strcspn(linea, "\r\n")] = '\0';
        list_add(lista, strdup(linea));
    }

    free(linea);   /* buffer interno de getline — siempre liberar aunque sea NULL */
    fclose(archivo);

    return lista;
}


char* obtener_linea_de_archivo(uint32_t pid, uint32_t pc) {
    char* pid_key = string_itoa(pid);

    pthread_mutex_lock(&mutex_procesos);
    t_contexto_proceso* ctx = dictionary_get(procesos, pid_key);
    pthread_mutex_unlock(&mutex_procesos);

    free(pid_key);

    if (ctx == NULL || ctx->instrucciones == NULL) {
        log_error(logger, "PID %u: contexto o lista de instrucciones no encontrada en Fetch", pid);
        return NULL;
    }

    if ((int)pc >= list_size(ctx->instrucciones)) {
        return NULL;   /* PC fuera de rango → fin de programa */
    }

    return strdup((char*)list_get(ctx->instrucciones, (int)pc));
}

t_memory_stick* buscar_ms_por_direccion(uint32_t dir_fisica, uint32_t* dir_local) {
    for(int i = 0; i < list_size(lista_memory_sticks); i++) {
        t_memory_stick* ms = list_get(lista_memory_sticks, i);
        
        if(dir_fisica >= ms->base_global && dir_fisica <= ms->limite_global) {
            *dir_local = dir_fisica - ms->base_global;
            return ms;
        }
    }
    return NULL; // Si devuelve NULL es porque la dirección no existe (SEG_FAULT físico)
}

bool escribir_memoria_fisica(uint32_t dir_fisica, uint32_t tamanio, void* datos) {
    uint32_t bytes_escritos = 0;
    while(bytes_escritos < tamanio) {
        uint32_t dir_local;
        t_memory_stick* ms = buscar_ms_por_direccion(dir_fisica + bytes_escritos, &dir_local);
        if(!ms) {
            log_error(logger, "Fallo de segmentación: dirección física %u fuera de rango", dir_fisica + bytes_escritos);
            return false;
        }

        uint32_t bytes_disponibles = ms->limite_global - (dir_fisica + bytes_escritos) + 1;
        uint32_t a_escribir = (tamanio - bytes_escritos < bytes_disponibles) ? (tamanio - bytes_escritos) : bytes_disponibles;

        pthread_mutex_lock(&ms->mutex_fd);
        t_paquete* paq = crear_paquete();
        paq->codigo_operacion = ESCRITURA_MEMORIA;
        agregar_a_paquete(paq, &dir_local, sizeof(uint32_t));
        agregar_a_paquete(paq, &a_escribir, sizeof(uint32_t));
        agregar_a_paquete(paq, (char*)datos + bytes_escritos, a_escribir);
        enviar_paquete(paq, ms->fd);
        eliminar_paquete(paq);

        int resp;
        int fd_ms = ms->fd;
        ssize_t leido = recv(fd_ms, &resp, sizeof(int), MSG_WAITALL); // Esperar OK
        pthread_mutex_unlock(&ms->mutex_fd);
        if (leido <= 0) {
            log_error(logger, "Memory Stick desconectado durante una escritura");
            desconectar_memory_stick(fd_ms);
            return false;
        }
        bytes_escritos += a_escribir;
    }
    return true;
}

void* leer_memoria_fisica(uint32_t dir_fisica, uint32_t tamanio) {
    void* buffer_final = malloc(tamanio);
    uint32_t bytes_leidos = 0;

    while(bytes_leidos < tamanio) {
        uint32_t dir_local;
        t_memory_stick* ms = buscar_ms_por_direccion(dir_fisica + bytes_leidos, &dir_local);
        if(!ms) {
            log_error(logger, "Fallo de segmentación en compactación (Lectura)");
            free(buffer_final);
            return NULL;
        }

        uint32_t bytes_disponibles = ms->limite_global - (dir_fisica + bytes_leidos) + 1;
        uint32_t a_leer = (tamanio - bytes_leidos < bytes_disponibles) ? (tamanio - bytes_leidos) : bytes_disponibles;

        pthread_mutex_lock(&ms->mutex_fd);
        t_paquete* paq = crear_paquete();
        paq->codigo_operacion = LECTURA_MEMORIA;
        agregar_a_paquete(paq, &dir_local, sizeof(uint32_t));
        agregar_a_paquete(paq, &a_leer, sizeof(uint32_t));
        enviar_paquete(paq, ms->fd);
        eliminar_paquete(paq);

        int fd_ms = ms->fd;
        int op_ms = recibir_operacion(fd_ms);
        if(op_ms == DATOS_LECTURA) {
            t_list* resp = recibir_paquete(fd_ms);
            pthread_mutex_unlock(&ms->mutex_fd);
            void* datos_recibidos = list_get(resp, 0);
            memcpy((char*)buffer_final + bytes_leidos, datos_recibidos, a_leer);
            list_destroy_and_destroy_elements(resp, free);
        } else {
            pthread_mutex_unlock(&ms->mutex_fd);
            if (op_ms == -1) {
                log_error(logger, "Memory Stick desconectado durante lectura de compactación");
                desconectar_memory_stick(fd_ms);
                free(buffer_final);
                return NULL;
            }
        }
        bytes_leidos += a_leer;
    }
    return buffer_final;
}

bool suspender_segmento(uint32_t pid, t_segmento* seg, int fd_swap) {
    uint32_t tamano_seg = seg->limite - seg->base + 1;
    uint32_t cant_bloques = (tamano_seg + tamano_bloque_swap - 1) / tamano_bloque_swap;

    void* datos = leer_memoria_fisica(seg->base, tamano_seg);
    if (!datos) {
        log_error(logger, "PID %u seg %u: fallo al leer memoria física para suspender", pid, seg->id);
        return false;
    }


    t_list* bloques_asignados = list_create();

    pthread_mutex_lock(&mutex_swap);

    for(uint32_t i = 0; i < cant_bloques; i++) {
        bool encontro_libre = false;
        uint32_t bloque_libre = 0;
        for(uint32_t b = 0; b < bitarray_get_max_bit(bitmap_swap); b++) {
            if(!bitarray_test_bit(bitmap_swap, b)) {
                bloque_libre = b;
                encontro_libre = true;
                bitarray_set_bit(bitmap_swap, b);
                break;
            }
        }

        if (!encontro_libre) {
            // SWAP lleno: antes esto seguia como si el bloque 0 estuviera libre,
            // pisando en silencio los datos de otro segmento ya suspendido ahi.
            // deshacemos los bloques que ya habiamos reservado en esta llamada
            log_error(logger, "PID %u seg %u: SWAP sin bloques libres, no se pudo suspender", pid, seg->id);
            for (int j = 0; j < list_size(bloques_asignados); j++) {
                uint32_t* b_reservado = list_get(bloques_asignados, j);
                bitarray_clean_bit(bitmap_swap, *b_reservado);
            }
            list_destroy_and_destroy_elements(bloques_asignados, free);
            pthread_mutex_unlock(&mutex_swap);
            free(datos);
            return false;
        }

        uint32_t* nro_bloque = malloc(sizeof(uint32_t));
        *nro_bloque = bloque_libre;
        list_add(bloques_asignados, nro_bloque);

        int op = ESCRITURA_BLOQUE;
        send(fd_swap, &op, sizeof(int), 0);
        send(fd_swap, &bloque_libre, sizeof(uint32_t), 0);

//Antes (bug): mandaba menos bytes que tamano_bloque_swap para el ultimo bloque parcial de un segmento pero swap siempre hace recv(fd_memoria, datos, block_size, MSG_WAITALL) esperando el bloque COMPLETO. Con un segmento que no es multiplo exacto de ese tamaño , swap se queda esperando para siempre
/*    uint32_t bytes_a_mandar = tamano_bloque_swap;
        if ((i * tamano_bloque_swap) + tamano_bloque_swap > tamano_seg) {
            bytes_a_mandar = tamano_seg - (i * tamano_bloque_swap);
        }
        send(fd_swap, (char*)datos + (i * tamano_bloque_swap), bytes_a_mandar, 0);        */ 

//Arreglo: siempre mandamos un bloque completo. si no llega a llenar el ultimo bloque se rellena con ceros 
        uint32_t bytes_reales = tamano_bloque_swap;
        if ((i * tamano_bloque_swap) + tamano_bloque_swap > tamano_seg) {
            bytes_reales = tamano_seg - (i * tamano_bloque_swap);
        }

        void* bloque_a_enviar = (char*)datos + (i * tamano_bloque_swap);
        void* buffer_relleno = NULL;
        if (bytes_reales < tamano_bloque_swap) {
            buffer_relleno = calloc(1, tamano_bloque_swap);
            memcpy(buffer_relleno, bloque_a_enviar, bytes_reales);
            bloque_a_enviar = buffer_relleno;
        }
        send(fd_swap, bloque_a_enviar, tamano_bloque_swap, 0);
        free(buffer_relleno);

        int resp;
        recv(fd_swap, &resp, sizeof(int), MSG_WAITALL);
    }
    free(datos);

    // 1. Guardar el mapa de bloques (Clave: "PID_IDSEGMENTO")
    char* clave = string_from_format("%u_%u", pid, seg->id);
    dictionary_put(mapa_bloques_swap, clave, bloques_asignados);
    free(clave);

    pthread_mutex_unlock(&mutex_swap);

    // 2. Liberar memoria física
    pthread_mutex_lock(&mutex_huecos);
    t_hueco* nuevo_hueco = malloc(sizeof(t_hueco));
    nuevo_hueco->base = seg->base;
    nuevo_hueco->tamano = tamano_seg;
    nuevo_hueco->limite = seg->base + tamano_seg - 1;
    list_add(huecos_libres, nuevo_hueco);
    consolidar_huecos();
    pthread_mutex_unlock(&mutex_huecos);

    return true;
}


bool verificar_espacio_para_desuspension(t_list* tabla_segmentos) {
    int      n_huecos = list_size(huecos_libres);
    uint32_t* sim     = malloc(n_huecos * sizeof(uint32_t));

    for (int i = 0; i < n_huecos; i++) {
        sim[i] = ((t_hueco*)list_get(huecos_libres, i))->tamano;
    }

    char* algoritmo = config_get_string_value(config, "ALLOCATION_STRATEGY");
    bool  resultado = true;

    for (int s = 0; s < list_size(tabla_segmentos); s++) {
        t_segmento* seg = list_get(tabla_segmentos, s);
        uint32_t    tam = seg->limite - seg->base + 1;
        int         elegido = -1;

        for (int h = 0; h < n_huecos; h++) {
            if (sim[h] >= tam) {
                if (elegido == -1) {
                    elegido = h;
                } else if (strcmp(algoritmo, "BEST") == 0 && sim[h] < sim[elegido]) {
                    elegido = h;
                } else if (strcmp(algoritmo, "WORST") == 0 && sim[h] > sim[elegido]) {
                    elegido = h;
                }
            }
        }

        if (elegido == -1) {
            resultado = false;
            break;
        }
        sim[elegido] -= tam; /* consumir el hueco elegido en la simulación */
    }

    free(sim);
    return resultado;
}

void liberar_bloques_swap(uint32_t pid, uint32_t id_segmento) {
    char* clave = string_from_format("%u_%u", pid, id_segmento);

    pthread_mutex_lock(&mutex_swap);
    t_list* bloques = dictionary_remove(mapa_bloques_swap, clave);
    free(clave);

    if (bloques == NULL) {
        pthread_mutex_unlock(&mutex_swap);
        return; /* El segmento no estaba en SWAP */
    }

    for (int i = 0; i < list_size(bloques); i++) {
        uint32_t* nro_bloque = list_get(bloques, i);
        bitarray_clean_bit(bitmap_swap, *nro_bloque);
    }
    pthread_mutex_unlock(&mutex_swap);

    list_destroy_and_destroy_elements(bloques, free);
}

void liberar_huecos_de_proceso(t_contexto_proceso* ctx) {
    if (ctx == NULL || ctx->tabla_segmentos == NULL) return;

    for (int i = 0; i < list_size(ctx->tabla_segmentos); i++) {
        t_segmento* seg = list_get(ctx->tabla_segmentos, i);

        char* clave = string_from_format("%u_%u", ctx->pid, seg->id);
        pthread_mutex_lock(&mutex_swap);
        bool estaba_en_swap = dictionary_has_key(mapa_bloques_swap, clave);
        pthread_mutex_unlock(&mutex_swap);
        free(clave);

        if (estaba_en_swap) {
            /* El segmento estaba suspendido: liberar sus bloques de SWAP */
            liberar_bloques_swap(ctx->pid, seg->id);
        } else {
            /* El segmento estaba en memoria física: devolver el espacio como hueco libre */
            uint32_t tamano_seg = seg->limite - seg->base + 1;

            pthread_mutex_lock(&mutex_huecos);
            t_hueco* nuevo_hueco = malloc(sizeof(t_hueco));
            nuevo_hueco->base = seg->base;
            nuevo_hueco->limite = seg->limite;
            nuevo_hueco->tamano = tamano_seg;
            list_add(huecos_libres, nuevo_hueco);
            consolidar_huecos();
            pthread_mutex_unlock(&mutex_huecos);
        }
    }
}

bool reservar_hueco_para_segmento(t_segmento* seg, uint32_t tamano_seg) {
    t_hueco* hueco = buscar_hueco_libre(tamano_seg);

    if (hueco == NULL) {
        return false;
    }

    seg->base   = hueco->base;
    seg->limite = hueco->base + tamano_seg - 1;
    hueco->base   += tamano_seg;
    hueco->tamano -= tamano_seg;

    if (hueco->tamano == 0) {
        for (int i = 0; i < list_size(huecos_libres); i++) {
            if (list_get(huecos_libres, i) == hueco) {
                list_remove(huecos_libres, i);
                free(hueco);
                break;
            }
        }
    }
    return true;
}

void liberar_hueco_reservado(t_segmento* seg, uint32_t tamano_seg) {
    t_hueco* nuevo_hueco = malloc(sizeof(t_hueco));
    nuevo_hueco->base   = seg->base;
    nuevo_hueco->limite = seg->base + tamano_seg - 1;
    nuevo_hueco->tamano = tamano_seg;
    list_add(huecos_libres, nuevo_hueco);
    consolidar_huecos();
}

bool desuspender_segmento(uint32_t pid, t_segmento* seg, int fd_swap) {
    char* clave = string_from_format("%u_%u", pid, seg->id);

    pthread_mutex_lock(&mutex_swap);
    t_list* bloques = dictionary_remove(mapa_bloques_swap, clave);
    pthread_mutex_unlock(&mutex_swap);
    free(clave);

    if (bloques == NULL) {
        log_error(logger, "PID %u seg %u: no se encontraron bloques en SWAP", pid, seg->id);
        return false;
    }

    uint32_t tamano_seg = seg->limite - seg->base + 1;

    void* buffer_segmento = malloc(tamano_seg);
    uint32_t offset = 0;

    pthread_mutex_lock(&mutex_swap);
    for(int i = 0; i < list_size(bloques); i++) {
        uint32_t* nro_bloque = list_get(bloques, i);

        int op = LECTURA_BLOQUE;
        send(fd_swap, &op, sizeof(int), 0);
        send(fd_swap, nro_bloque, sizeof(uint32_t), 0);

        void* datos_bloque = malloc(tamano_bloque_swap);
        recv(fd_swap, datos_bloque, tamano_bloque_swap, MSG_WAITALL);

        uint32_t bytes_a_copiar = tamano_bloque_swap;
        if (offset + tamano_bloque_swap > tamano_seg) {
            bytes_a_copiar = tamano_seg - offset;
        }

        memcpy((char*)buffer_segmento + offset, datos_bloque, bytes_a_copiar);
        offset += bytes_a_copiar;
        free(datos_bloque);

        bitarray_clean_bit(bitmap_swap, *nro_bloque);
    }
    pthread_mutex_unlock(&mutex_swap);

    // Escribimos a los MS
    escribir_memoria_fisica(seg->base, tamano_seg, buffer_segmento);
    
    free(buffer_segmento);
    list_destroy_and_destroy_elements(bloques, free);
    return true;
}