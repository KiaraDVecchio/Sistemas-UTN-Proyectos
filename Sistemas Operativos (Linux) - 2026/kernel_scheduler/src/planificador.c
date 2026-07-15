#include "planificador.h"
#include "sockets.h"
#include "cpu_handler.h"
#include "io_handler.h"
#include "mutex_handler.h"
#include "kernel_scheduler.h"
#include "compactacion_handler.h"
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <unistd.h>

//var estaticas
t_list* todas_las_cpus = NULL;

//lista compartida entre hilos, ojo
static t_list* cpus_parciales = NULL; // a las que solo le llego el dispatch
static pthread_mutex_t mutex_parciales = PTHREAD_MUTEX_INITIALIZER;

typedef struct{
    uint32_t pid;
    int fd_interrupt;
} t_quantum_args; //datos para pasarle al hilo


static t_cpu* buscar_parcial(uint32_t id){
    for (int i = 0; i < list_size(cpus_parciales); i++) {
        t_cpu* cpu = list_get(cpus_parciales, i);
        if (cpu->id == id) return cpu;
    }
    return NULL;
}

//mandamos un proceso a ejecutar a cpu
static void despachar_a_cpu(t_proceso* proceso, t_cpu* cpu) {
    t_paquete* paquete = crear_paquete();
    cpu->libre = false;
    cpu->pid_actual = proceso->pid;
    cpu->prioridad_proceso = proceso->prioridad;
    paquete->codigo_operacion = DESPACHAR_PROCESO;

    //armamos paquete con los datos del proceso
    agregar_a_paquete(paquete, &proceso->pid,sizeof(uint32_t));

    enviar_paquete(paquete, cpu->fd_dispatch);

    eliminar_paquete(paquete);
}

static void* hilo_quantum(void* arg){ //mientras la cpu ejecuta
    t_quantum_args* args = arg;

    struct timespec ts={
        .tv_sec= quantum/1000,
        .tv_nsec = (long) (quantum%1000)*1000000L
    };
    nanosleep(&ts,NULL); //hilo duerme mientras corrre el quantum

    t_proceso* proceso= buscar_proceso_por_pid(args->pid);
    if(proceso!=NULL && proceso->estado == EXEC) {
        enviar_interrupcion(args->fd_interrupt, args->pid);
    }

    free(args);
    return NULL;
}

static void iniciar_quantum(t_proceso* proceso, t_cpu* cpu){
    t_quantum_args* args = malloc(sizeof(t_quantum_args));
    args->pid = proceso->pid;
    args->fd_interrupt = cpu->fd_interrupt;

    pthread_t hilo;
    pthread_create(&hilo,NULL,hilo_quantum,args);
    pthread_detach(hilo);

}

static void manejar_cpu_dispatch(t_cpu* cpu) {
    while (1) {
        int op = recibir_operacion(cpu->fd_dispatch);
        if (op == -1) {
            log_warning(logger, "CPU %d desconectada", cpu->id);
            break;
        }

        switch (op) {
        case DESALOJO_CPU: {
            t_list* datos = recibir_paquete(cpu->fd_dispatch);
            uint32_t pid  = *(uint32_t*)list_get(datos, 0);
            t_codigo_instruccion motivo = *(t_codigo_instruccion*)list_get(datos, 1);
            t_proceso* proceso = buscar_proceso_por_pid(pid);

            bool requiere_compactacion = false;
            uint32_t pid_alloc     = pid;
            uint32_t tamanio_alloc = 0;
            uint32_t id_seg_alloc  = 0;

            switch (motivo) {
                case EXIT_P:
                    log_info(logger, "## (%d) finalizó su ejecución con motivo de EXIT_PROCESS", pid);
                    if (proceso != NULL) {
                        cambiar_estado(proceso, EXIT);
                        mutex_liberar_de_proceso(proceso);
                        //avisamos al memory que tiene que liberar porque el proceso termino
                        pthread_mutex_lock(&mutex_fd_kernel_memory);
                        t_paquete* paq_exit = crear_paquete();
                        paq_exit->codigo_operacion = FINALIZAR_PROCESO;
                        agregar_a_paquete(paq_exit, &pid, sizeof(uint32_t));
                        enviar_paquete(paq_exit, fd_kernel_memory);
                        eliminar_paquete(paq_exit);
                        pthread_mutex_unlock(&mutex_fd_kernel_memory);
                        intentar_dessuspender(); //como liberamos memoria chequeamos si se puede desuspender alguno
                        intentar_reintentar_mem_alloc_pendientes();
                    }
                    break;

                case INTERRUPCION_QUANTUM:
                    if (compactando) {
                        if (proceso != NULL) agregar_primero_ready(proceso);
                    } else {
                        log_info(logger, "## (%d) - Desalojado por fin de quantum", pid);
                        if (proceso != NULL) cambiar_estado(proceso, READY);
                    }
                    break;

                case SLEEP:
                case STDOUT:
                case STDIN: {
                    uint32_t param1 = 0, param2 = 0;
                    if (list_size(datos) > 2) param1 = *(uint32_t*)list_get(datos, 2);
                    if (list_size(datos) > 3) param2 = *(uint32_t*)list_get(datos, 3);
                    t_cod_op tipo_io = (motivo == SLEEP)  ? IO_SLEEP  :
                                    (motivo == STDOUT) ? IO_STDOUT : IO_STDIN;
                    if (proceso != NULL)
                        despachar_io(proceso, tipo_io, param1, param2);
                    break;
                }

                case MUTEX_CREATE:
                case MUTEX_LOCK:
                case MUTEX_UNLOCK: {
                    char* nombre_mutex = (char*)list_get(datos, 3);
                    if (proceso != NULL) {
                        if (motivo == MUTEX_CREATE)      mutex_create(proceso, nombre_mutex, cpu);
                        else if (motivo == MUTEX_LOCK)   mutex_lock(proceso, nombre_mutex, cpu);
                        else if (motivo == MUTEX_UNLOCK) mutex_unlock(proceso, nombre_mutex, cpu);
                    }
                    break;
                }

                case MEM_ALLOC: {
                    log_info(logger, "## (%d) - Solicitó syscall: MEM_ALLOC", pid);
                    if (list_size(datos) > 2)
                        id_seg_alloc  = *(uint32_t*)list_get(datos, 2);
                    if (list_size(datos) > 3)
                        tamanio_alloc = *(uint32_t*)list_get(datos, 3);
                    if (proceso != NULL) {
                        pthread_mutex_lock(&mutex_fd_kernel_memory);
                        t_paquete* paq = crear_paquete();
                        paq->codigo_operacion = MEM_ALLOC;
                        uint32_t es_reintento = 0; // primer intento: puede disparar compactación
                        agregar_a_paquete(paq, &pid,           sizeof(uint32_t));
                        agregar_a_paquete(paq, &id_seg_alloc,  sizeof(uint32_t));
                        agregar_a_paquete(paq, &tamanio_alloc, sizeof(uint32_t));
                        agregar_a_paquete(paq, &es_reintento,  sizeof(uint32_t));
                        enviar_paquete(paq, fd_kernel_memory);
                        eliminar_paquete(paq);
                        int resp = recibir_operacion(fd_kernel_memory);
                        pthread_mutex_unlock(&mutex_fd_kernel_memory);
                        if (resp == SOLICITUD_COMPACTACION)
                            requiere_compactacion = true;
                        else
                            cambiar_estado(proceso, READY);
                    }
                    break;
                }

                case MEM_FREE: {
                    log_info(logger, "## (%d) - Solicitó syscall: MEM_FREE", pid);
                    if (proceso != NULL && list_size(datos) > 2) {
                        uint32_t id_seg = *(uint32_t*)list_get(datos, 2);
                        pthread_mutex_lock(&mutex_fd_kernel_memory);
                        t_paquete* paq = crear_paquete();
                        paq->codigo_operacion = MEM_FREE;
                        agregar_a_paquete(paq, &pid,    sizeof(uint32_t));
                        agregar_a_paquete(paq, &id_seg, sizeof(uint32_t));
                        enviar_paquete(paq, fd_kernel_memory);
                        eliminar_paquete(paq);
                        recibir_operacion(fd_kernel_memory);
                        pthread_mutex_unlock(&mutex_fd_kernel_memory);
                        cambiar_estado(proceso, READY);
                        intentar_dessuspender();//despues de la syscall mem_free revisamos si podemos desuspender algun proceso
                        intentar_reintentar_mem_alloc_pendientes();
                    }
                    break;
                }

                case INIT_PROC: {
                    log_info(logger, "## (%d) - Solicitó syscall: INIT_PROC", pid);
                    // paquete: [pid, motivo, largo_str, path, prioridad]
                    if (proceso != NULL && list_size(datos) > 4) {
                        char* path_hijo    = (char*)list_get(datos, 3);
                        uint32_t prio_hijo = *(uint32_t*)list_get(datos, 4);
                        if (strcmp(algoritmo_de_planificacion, "CMN") == 0 && (int)prio_hijo >= cant_colas) { //el usuario puede haber puesto cualquier numero en prioridad, hay que ajustar
                            log_warning(logger, "## (%d) INIT_PROC: prioridad %u fuera de rango (max %d), ajustando al maximo válido",
                                        pid, prio_hijo, cant_colas - 1);
                            prio_hijo = (uint32_t)(cant_colas - 1); //se agrega al ultimo espacio valido en la cola
                        }
                        t_proceso* hijo    = crear_proceso(path_hijo, (int)prio_hijo);

                        pthread_mutex_lock(&mutex_fd_kernel_memory);
                        t_paquete* paq = crear_paquete();
                        paq->codigo_operacion = CREAR_PROCESO;
                        agregar_a_paquete(paq, &hijo->pid, sizeof(uint32_t));
                        uint32_t path_len = (uint32_t)strlen(path_hijo) + 1;
                        agregar_a_paquete(paq, path_hijo, path_len);
                        enviar_paquete(paq, fd_kernel_memory);
                        eliminar_paquete(paq);
                        recibir_operacion(fd_kernel_memory);
                        pthread_mutex_unlock(&mutex_fd_kernel_memory);

                        cambiar_estado(hijo,    READY);
                        cambiar_estado(proceso, READY);
                    }
                    break;
                }

                case SEGMENTATION_FAULT:
                    log_info(logger, "## (%d) finalizó su ejecución con motivo de SEGMENTATION_FAULT", pid);
                    if (proceso != NULL) {
                        cambiar_estado(proceso, EXIT);
                        mutex_liberar_de_proceso(proceso);
                        pthread_mutex_lock(&mutex_fd_kernel_memory);
                        t_paquete* paq_segfault = crear_paquete();
                        paq_segfault->codigo_operacion = FINALIZAR_PROCESO;
                        agregar_a_paquete(paq_segfault, &pid, sizeof(uint32_t));
                        enviar_paquete(paq_segfault, fd_kernel_memory);
                        eliminar_paquete(paq_segfault);
                        pthread_mutex_unlock(&mutex_fd_kernel_memory);
                        //despues de liberar mem del proceso que termino, revisamos si se puede desuspender algun proceso
                        intentar_dessuspender();
                        intentar_reintentar_mem_alloc_pendientes();

                    }
                    break;

                default:
                    log_warning(logger, "## (%d) - Motivo de desalojo desconocido: %d", pid, motivo);
                    break;
            }

            list_destroy_and_destroy_elements(datos, free);
            cpu_disponible(cpu);

            // compactacion inline: la hacemos despues de cpu_disponible para que
            // esta cpu cuente como libre y el wait de ejecutar_compactacion no se trabe
            if (requiere_compactacion) {
                ejecutar_compactacion();
                pthread_mutex_lock(&mutex_fd_kernel_memory);
                t_paquete* paq = crear_paquete();
                paq->codigo_operacion = MEM_ALLOC;
                uint32_t es_reintento = 1; // ya compactamos por este pedido, no pedir de nuevo
                agregar_a_paquete(paq, &pid_alloc,     sizeof(uint32_t));
                agregar_a_paquete(paq, &id_seg_alloc,  sizeof(uint32_t));
                agregar_a_paquete(paq, &tamanio_alloc, sizeof(uint32_t));
                agregar_a_paquete(paq, &es_reintento,  sizeof(uint32_t));
                enviar_paquete(paq, fd_kernel_memory);
                eliminar_paquete(paq);
                int resp_retry = recibir_operacion(fd_kernel_memory);
                pthread_mutex_unlock(&mutex_fd_kernel_memory);
                t_proceso* proc_retry = buscar_proceso_por_pid(pid_alloc);
                if (proc_retry != NULL) {
                    if (resp_retry == RESPUESTA_OK) {
                        cambiar_estado(proc_retry, READY);
                    } else {
                        // ni compactando entró: no tiene sentido pedir otra compactación,
                        // se bloquea hasta que se libere memoria en algún otro lado.
                        log_warning(logger, "## (%d) no consiguió memoria ni compactando, queda BLOCK esperando espacio para el segmento %u (tamaño %u)",
                                    pid_alloc, id_seg_alloc, tamanio_alloc);
                        proc_retry->esperando_mem_alloc = true;
                        proc_retry->id_seg_pendiente = id_seg_alloc;
                        proc_retry->tamanio_pendiente = tamanio_alloc;
                        cambiar_estado(proc_retry, BLOCK);
                    }
                }
            }
            break;
        }

        default:
            log_warning(logger, "Operacion desconocida %d de CPU %d", op, cpu->id);
            break;
        }
    }
}


static void* hilo_planificador(void * arg){
    (void)arg;
    while(1){
        pthread_mutex_lock(&mutex_estado);

        bool hay_proceso = false;
        while(!hay_proceso || list_is_empty(cpus_libres) || compactando){
            if(strcmp(algoritmo_de_planificacion, "CMN") == 0) {
                hay_proceso = false;
                for(int i=0; i<cant_colas; i++) {
                    if(!list_is_empty(colas_ready_multinivel[i])){ //busco algun proceso recorriendo todas las colas
                        hay_proceso = true;
                        break;
                    }
                }
            }else{
                hay_proceso = !list_is_empty(cola_ready);//si no es multicolas solo miro si hay algun proc en ready
            }
            if(!hay_proceso || list_is_empty(cpus_libres) || compactando) //tambien dormimos si hay compactacion en curso
                pthread_cond_wait(&cond_planificador, &mutex_estado);
        }

        //elegimos el proceso
        t_proceso* proceso = NULL;
        if(strcmp(algoritmo_de_planificacion, "CMN") == 0) {
            for(int i=0; i<cant_colas; i++){ // lo corregi, hay q buscar desde la priori 0 para arriba!!
                if(!list_is_empty(colas_ready_multinivel[i])){
                    proceso = list_get(colas_ready_multinivel[i],0);
                    break;
                }
            }
        } else {
            proceso = list_get(cola_ready, 0); //ojo no estas sacando todavia, eso se hace en cambiar estado, por eso me tiraba error
        }

        t_cpu* cpu = list_remove(cpus_libres, 0);
        pthread_mutex_unlock(&mutex_estado);

        cambiar_estado(proceso, EXEC);
        proceso->fd_cpu = cpu->fd_dispatch;
        log_info(logger, "## Se despacha proceso %d", proceso->pid);
        despachar_a_cpu(proceso, cpu);

        //quantum:en CMN miramos el algoritmo de esa cola
        bool usar_rr = false;
        if(strcmp(algoritmo_de_planificacion,"RR") == 0) {
            usar_rr = true;
        }else if(strcmp(algoritmo_de_planificacion,"CMN") == 0) {
            if(strcmp(algoritmo_por_cola[proceso->prioridad],"RR") == 0) //vemos el algoritmo de la cola del proceso
                usar_rr = true;
        }
        if(usar_rr) {
            iniciar_quantum(proceso, cpu);
        }
        //y si es fifo lo dejamos correr
    }
    return NULL;
}

void cpu_disponible(t_cpu* cpu){ //despues de exit o fin de quantum
    pthread_mutex_lock(&mutex_estado);
    list_add(cpus_libres, cpu);
    pthread_cond_signal(&cond_planificador);//avisamos al planificador que hay una cpu libre
    pthread_mutex_unlock(&mutex_estado);
    cpu->libre = true;
}

void iniciar_planificador(void) {
    cpus_parciales = list_create();
    todas_las_cpus = list_create();
    pthread_t hilo;
    pthread_create(&hilo, NULL, hilo_planificador, NULL);
    pthread_detach(hilo);
}

void registrar_cpu_dispatch(int fd){
    uint32_t id;
    recv(fd, &id, sizeof(uint32_t), MSG_WAITALL);

    pthread_mutex_lock(&mutex_parciales);
    t_cpu* cpu = buscar_parcial(id); //busco si solo tiene 1 conexion (interrupt) o ninguna
    if(cpu==NULL){ //si no tiene ninguna conexion todavia, registramos dispatch
        cpu = malloc(sizeof(t_cpu));
        cpu->id =id;
        cpu->fd_dispatch = fd;
        cpu->fd_interrupt = -1;
        list_add(cpus_parciales,cpu);

    } else{
        cpu->fd_dispatch = fd; //si solo tiene interrupt (no deberia xq deberia llegar primero dispatch desde cpu, pero puede pasar) completamos dispatch
    }
    pthread_mutex_unlock(&mutex_parciales);

    while(cpu->fd_interrupt==-1) usleep(5000); //esperamos el socket interrupt y vamos preguntando cada 5ms

    log_info(logger, "## CPU %d Conectada", id);
    list_add(todas_las_cpus,cpu);
    cpu_disponible(cpu);
    manejar_cpu_dispatch(cpu); // recibimos las respuestas de la CPU. Se bloquea el hilo y detecta desconexión.

}

void registrar_cpu_interrupt(int fd){
    uint32_t id;
    recv(fd,&id, sizeof(uint32_t), MSG_WAITALL);
    pthread_mutex_lock(&mutex_parciales);
    t_cpu* cpu = buscar_parcial(id); //busco si solo tiene 1 conexion (dispatch) o ninguna
    if(cpu==NULL){
        cpu = malloc(sizeof(t_cpu));
        cpu->id = id;
        cpu->fd_dispatch = -1;
        cpu->fd_interrupt = fd; //si no tiene ninguna asigno interrupt
        list_add(cpus_parciales, cpu);

    }else{
        cpu->fd_interrupt=fd; //si solo tiene dispatch, registro interrupt
    }
    pthread_mutex_unlock(&mutex_parciales);
    //el scheduler escribe asi que usamos recv para bloquearnos y esperar hasta q la CPU se desconecte (nosotros no recibimos nada)
    char dummy; //porque no nos interesa lo que llega
    while (recv(fd, &dummy, 1, 0) > 0);

}

void verificar_desalojo_por_prioridad(t_proceso* nuevo_proceso){
    if(!queue_preemption){
        return;
    }

    if(strcmp(algoritmo_de_planificacion, "CMN") != 0){
        return;
    }

    pthread_mutex_lock(&mutex_estado);
    for(int i=0; i<list_size(todas_las_cpus); i++){
        t_cpu* cpu = list_get(todas_las_cpus, i);
        if (cpu->libre){
            continue; //solo me importan las que tengan un proceso ejecutando
        }

        //por cada proceso ejecutando compara su priori con la del nuevo q llego para chequear si hay que desalojar
        if(cpu->prioridad_proceso > nuevo_proceso->prioridad){ //si la priori del nuevo es mas chica es porque es mas urgente, se desaloja
            log_info(logger, "## (%d) Prioridad: %d - Desalojado por cola mas prioritaria por el proceso %d con prioridad %d",
                    cpu->pid_actual, cpu->prioridad_proceso,
                    nuevo_proceso->pid, nuevo_proceso->prioridad);
            pthread_mutex_unlock(&mutex_estado);
            enviar_interrupcion(cpu->fd_interrupt, cpu->pid_actual); //se desaloja la primera cpu que tiene un proceso menos prioritario que el nuevo
            return;
        }
    }
    pthread_mutex_unlock(&mutex_estado);
    
}

