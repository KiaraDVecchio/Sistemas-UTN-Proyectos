#include "kernel_memory.h"
#include <signal.h>

int main(int argc, char* argv[]) {
    signal(SIGPIPE, SIG_IGN);
    if (argc < 2) {
        fprintf(stderr, "ERROR: Falta el archivo de configuración\n");
        return EXIT_FAILURE;
    }

    return iniciar_memoria(argv[1]);
} 
