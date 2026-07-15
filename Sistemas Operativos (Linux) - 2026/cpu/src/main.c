#include <stdlib.h>
#include "cpu.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <commons/log.h>
#include "sockets.h"
#include "comunicacion.h"
#include <pthread.h>
#include <signal.h>





int main(int argc, char* argv[]) {
    signal(SIGPIPE, SIG_IGN);
    if (argc < 3) {
            fprintf(stderr, "Uso incorrecto. Ejecutar como: ./bin/cpu [Config] [Arg2]\n");
            return EXIT_FAILURE;
        }

    iniciar_cpu(argv[1], argv[2]);
    
    //Creacion del hilo
    pthread_t hilo_interrupcion;
    
    pthread_create(&hilo_interrupcion, NULL, espera_interrupcion, NULL);

    ejecucionDeProceso(); //capaz cambiar nombre porque aca tambien lo espera

    finalizar_cpu();

    log_destroy(logger);
    return EXIT_SUCCESS;
}