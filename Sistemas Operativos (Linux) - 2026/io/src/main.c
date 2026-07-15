#include <stdlib.h>
#include <signal.h>
#include <io.h>

int main(int argc, char* argv[]) {
    signal(SIGPIPE, SIG_IGN);
    // Se esperan 3 argumentos: ./bin/io [Archivo Config] [Nombre/Tipo]
    if (argc < 3) {
        fprintf(stderr, "Uso: %s [Archivo Config] [Nombre Interfaz]\n", argv[0]);
        return EXIT_FAILURE;
    }

    char* config_path = argv[1];
    char* nombre_interfaz = argv[2];

    return iniciar_io(config_path, nombre_interfaz);
}