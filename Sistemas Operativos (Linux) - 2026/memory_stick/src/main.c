#include "memory_stick.h"
#include <signal.h>
#define TAMANIO_MEMORIA 1024

int main(int argc, char* argv[]) {
    signal(SIGPIPE, SIG_IGN);
    if (argc < 3) {
        fprintf(stderr, "Uso: %s [Config] [Tamaño]\n", argv[0]);
        return EXIT_FAILURE;
    }

    iniciar_memory_stick(argv[1], argv[2]);
    
    finalizar_memory_stick();

    return EXIT_SUCCESS;
}