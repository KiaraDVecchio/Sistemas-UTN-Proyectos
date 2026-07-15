#include <stdlib.h> // Agregado para EXIT_FAILURE
#include <signal.h>

#include <kernel_scheduler.h>
#include <cpu_handler.h>
int main(int argc, char* argv[]) {
    signal(SIGPIPE, SIG_IGN);
    printf("Iniciando binario del Kernel...\n"); // Si no ves esto, el problema es cómo lo ejecutás.
    if (argc < 3) {
        printf("ERROR: Falta pasar el archivo de configuración por parámetro.\n");
        printf("ERROR: Uso: %s [Config] [Path Proceso Inicial]\n", argv[0]);
        return EXIT_FAILURE;
    }

    return iniciar_kernel(argv[1],argv[2]);

}