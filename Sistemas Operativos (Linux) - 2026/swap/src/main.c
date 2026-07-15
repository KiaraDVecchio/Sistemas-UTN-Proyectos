#include "swap.h"
#include <signal.h>

int main(int argc, char* argv[]) {
    signal(SIGPIPE, SIG_IGN);
    if (argc < 2) {
        fprintf(stderr, "ERROR: Falta pasar el archivo de configuración por parámetro\n");
        return EXIT_FAILURE;
    }

    return iniciar_swap(argv[1]);
}