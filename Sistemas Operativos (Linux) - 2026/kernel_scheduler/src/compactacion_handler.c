#include "compactacion_handler.h"
#include "kernel_scheduler.h"
#include "planificador.h"
#include "proceso.h"
#include "cpu_handler.h"
#include "io_handler.h"
#include <sys/poll.h>
#include <commons/log.h>

void ejecutar_compactacion(void) {
    pthread_mutex_lock(&mutex_estado);
    compactando = true;
    log_info(logger, "## Inicio de compactación");

    // copiamos los punteros de las cpus ocupadas para soltar el mutex antes de interrumpir
    t_list* ocupadas = list_create();
    for (int i = 0; i < list_size(todas_las_cpus); i++) {
        t_cpu* cpu = list_get(todas_las_cpus, i);
        if (!cpu->libre) list_add(ocupadas, cpu);
    }
    pthread_mutex_unlock(&mutex_estado);

    for (int i = 0; i < list_size(ocupadas); i++) {
        t_cpu* cpu = list_get(ocupadas, i);
        enviar_interrupcion(cpu->fd_interrupt, cpu->pid_actual);
    }
    list_destroy(ocupadas);

    // esperamos a que todas las cpus hayan devuelto su proceso
    pthread_mutex_lock(&mutex_estado);
    while (list_size(cpus_libres) < list_size(todas_las_cpus))
        pthread_cond_wait(&cond_planificador, &mutex_estado);
    pthread_mutex_unlock(&mutex_estado);

    // le decimos a kernel memory que compacte
    pthread_mutex_lock(&mutex_fd_kernel_memory);
    int cod = EJECUTAR_COMPACTACION;
    send(fd_kernel_memory, &cod, sizeof(int), 0);
    recibir_operacion(fd_kernel_memory); // espera FIN_COMPACTACION
    pthread_mutex_unlock(&mutex_fd_kernel_memory);

    pthread_mutex_lock(&mutex_estado);
    compactando = false;
    log_info(logger, "## Fin de compactación");
    pthread_cond_broadcast(&cond_planificador);
    pthread_mutex_unlock(&mutex_estado);

    // la compactación pudo haber resuelto la fragmentación de algún pendiente
    intentar_reintentar_mem_alloc_pendientes();
}

// intenta dessuspender procesos en SUSP_READY si es hay memoria disponible
void intentar_dessuspender(void) {
    pthread_mutex_lock(&mutex_estado);
    if(list_is_empty(cola_susp_ready)){
        pthread_mutex_unlock(&mutex_estado);
        return;
    }

//Problema: usabamos list_sort con un comparador de prioridad para ordenar candidatos pero cuando habia empate el sort los intercambiaba mal asi que perdiamos el fifo (o sea si dos tenian la misma prioridad, la comparacion daba false porque comparaba 1<1 asi q se perdia fifo y quedaba al final el q estaba esperando hace mas
    // copiamos la lista para no tenerla bloqueada 
    t_list* candidatos = list_create();
    if (strcmp(algoritmo_de_planificacion, "CMN") == 0) { 
        for (int prio = 0; prio < cant_colas; prio++) {
            for (int i = 0; i < list_size(cola_susp_ready); i++) { //recorremos por cada prioridad (de menor a mayor) la lista de susp y si es la priori q buscamos lo agregamos a una lista de candidatos que nos va a quedar ordenada
                t_proceso* p = list_get(cola_susp_ready, i);
                if (p->prioridad == prio)
                    list_add(candidatos, p);
            }
        } //los mas prioritarios quedan primero, y si hay empate ya es fifo por naturaleza 
    } else {
    for (int i = 0; i < list_size(cola_susp_ready); i++)
        list_add(candidatos, list_get(cola_susp_ready, i));
    }

    // Los reservamos ya mismo, bajo el mismo lock: esta funcion se puede disparar
    // casi en simultaneo desde varios lugares
    // y si no los sacamos aca, dos llamadas concurrentes pueden
    // agarrar el mismo proceso de cola_susp_ready y despacharlo dos veces.
    for (int i = 0; i < list_size(candidatos); i++) {
        t_proceso* p = list_get(candidatos, i);
        for (int j = 0; j < list_size(cola_susp_ready); j++) {
            if (list_get(cola_susp_ready, j) == p) {
                list_remove(cola_susp_ready, j);
                break;
            }
        }
    }
    pthread_mutex_unlock(&mutex_estado);

    int procesados = 0;
    for (int i = 0; i < list_size(candidatos); i++) {
        t_proceso* proceso = list_get(candidatos, i);

        pthread_mutex_lock(&mutex_fd_kernel_memory);
        int cod = DESUSPENDER_PROCESO;
        send(fd_kernel_memory, &cod, sizeof(int), 0);
        send(fd_kernel_memory, &proceso->pid, sizeof(uint32_t), 0);
        int resp = recibir_operacion(fd_kernel_memory);
        pthread_mutex_unlock(&mutex_fd_kernel_memory);

        if (resp == DESUSPENSION_OK) {
            completar_stdin_pendiente(proceso);
            log_info(logger, "## (%d) finalizó IO y pasa a READY / SUSP. READY", proceso->pid);
            cambiar_estado(proceso, READY);
            procesados++;
        } else {
            // sin espacio
            break;
        }
    }

    // Los que no llegamos a des-suspender (por falta de espacio) vuelven a la cola.
    if (procesados < list_size(candidatos)) {
        pthread_mutex_lock(&mutex_estado);
        for (int i = procesados; i < list_size(candidatos); i++) {
            list_add(cola_susp_ready, list_get(candidatos, i));
        }
        pthread_mutex_unlock(&mutex_estado);
    }

    list_destroy(candidatos);
}

// Reintenta los MEM_ALLOC que quedaron bloqueados por falta de memoria (ni compactando
// entraban). No pide compactación de nuevo (es_reintento=1): solo aprovecha memoria que
// se haya liberado desde el último intento. Se llama en los mismos eventos que ya
// disparan intentar_dessuspender (fin de IO/MEM_FREE/fin de proceso/nuevo memory stick/
// fin de compactación), y también encaja bien invocarla junto a esos.
void intentar_reintentar_mem_alloc_pendientes(void) {
    pthread_mutex_lock(&mutex_block);
    t_list* candidatos = list_create();
    for (int i = 0; i < list_size(cola_block); i++) {
        t_proceso* p = list_get(cola_block, i);
        if (p->esperando_mem_alloc) list_add(candidatos, p);
    }
    pthread_mutex_unlock(&mutex_block);

    if (list_is_empty(candidatos)) {
        list_destroy(candidatos);
        return;
    }

    // orden simple FIFO (según cola_block); el enunciado no exige un criterio
    // de prioridad para este caso, a diferencia de la des-suspensión.
    for (int i = 0; i < list_size(candidatos); i++) {
        t_proceso* proceso = list_get(candidatos, i);

        pthread_mutex_lock(&mutex_fd_kernel_memory);
        t_paquete* paq = crear_paquete();
        paq->codigo_operacion = MEM_ALLOC;
        uint32_t es_reintento = 1; // nunca disparamos compactación desde acá
        agregar_a_paquete(paq, &proceso->pid,               sizeof(uint32_t));
        agregar_a_paquete(paq, &proceso->id_seg_pendiente,  sizeof(uint32_t));
        agregar_a_paquete(paq, &proceso->tamanio_pendiente, sizeof(uint32_t));
        agregar_a_paquete(paq, &es_reintento,               sizeof(uint32_t));
        enviar_paquete(paq, fd_kernel_memory);
        eliminar_paquete(paq);
        int resp = recibir_operacion(fd_kernel_memory);
        pthread_mutex_unlock(&mutex_fd_kernel_memory);

        if (resp == RESPUESTA_OK) {
            proceso->esperando_mem_alloc = false;
            log_info(logger, "## (%d) consiguió memoria para el segmento pendiente %u, pasa a READY",
                    proceso->pid, proceso->id_seg_pendiente);
            cambiar_estado(proceso, READY);
        }
        // si sigue sin espacio, se deja tal cual: sigue en BLOCK con el flag en true,
        // se reintentará en el próximo evento que libere memoria.
    }

    list_destroy(candidatos);
}

static void* hilo_escucha_kernel_memory(void* arg) {
    (void)arg;
    while(1){

        //el poll no lee nada del socket, no hace falta tener el mutex tomado para esto. antes lo agarraba antes del poll y retenia 100ms bloqueando a otros procesos
        struct pollfd pfd={ .fd = fd_kernel_memory, .events = POLLIN };
        int r = poll(&pfd, 1, 100);
        if (r <= 0 || !(pfd.revents & POLLIN)) {
            continue;
        }

         //recien aca tomamos el mutex si hay datos, y volvemos a chequear por si alguien mas leyo esos bytes en el rato que tardamos, asi no nos quedamos esperando un msj que no va a llegar
        pthread_mutex_lock(&mutex_fd_kernel_memory);
        struct pollfd pfd_confirm={ .fd = fd_kernel_memory, .events = POLLIN };
        int r_confirm = poll(&pfd_confirm, 1, 0);
        if (r_confirm <= 0 || !(pfd_confirm.revents & POLLIN)) {
            pthread_mutex_unlock(&mutex_fd_kernel_memory);
            continue;
        }

        int op = recibir_operacion(fd_kernel_memory);
        pthread_mutex_unlock(&mutex_fd_kernel_memory);

        if(op == -1){
            log_error(logger, "Kernel Memory desconectado");
            break;
        }

        if(op==SOLICITUD_COMPACTACION){
            ejecutar_compactacion();
        }else if (op==MEMORIA_AMPLIADA){ //o sea se conecto un stick
            intentar_dessuspender();
            intentar_reintentar_mem_alloc_pendientes();
            }else if (op==MEMORIA_CORRUPTA){ //se desconecto un stick
            log_error(logger, "## BSOD - Memory Stick desconectado. Finalizando todos los procesos.");
            pthread_mutex_lock(&mutex_estado);
            for (int i = 0; i < list_size(procesos); i++) {
                t_proceso* p = list_get(procesos, i);
                if (p->estado != EXIT) { //ojo seteo estado directamente asi porque si pongo cambiar_estado ese toma mutex_estado q ya tomamos y hay deadlock
                    //no pasa nada con las colas porque abajo termino el scheduler completo
                    log_info(logger, "## (%d) Pasa del estado %s al estado EXIT",
                            p->pid, nombre_estado(p->estado));
                    p->estado = EXIT;
                }
            }
            pthread_mutex_unlock(&mutex_estado);
            exit(EXIT_FAILURE);
        }else {
            log_warning(logger, "operacion inesperada de Kernel Memory: %d", op);
        }
    }
    return NULL;
}

void iniciar_escucha_kernel_memory(void){
    pthread_t hilo;
    pthread_create(&hilo, NULL, hilo_escucha_kernel_memory, NULL);
    pthread_detach(hilo);
}
