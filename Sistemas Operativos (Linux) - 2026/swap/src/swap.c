#include "swap.h"

t_log* logger;
t_config* config;
FILE* archivo_swap = NULL;

int iniciar_swap(char* config_path) {

    config = config_create(config_path);
    if (config == NULL) {
        printf("No se pudo crear el config en la ruta: %s\n", config_path);
        return EXIT_FAILURE;
    }

    logger = log_create("swap.log", "SWAP", true, LOG_LEVEL_INFO);

    inicializar_archivo_swap();

    char* ip_m = config_get_string_value(config, "IP_MEMORY");
    char* puerto_m = config_get_string_value(config, "PUERTO_MEMORY");

    log_info(logger, "Intentando conectar a Kernel Memory en %s:%s", ip_m, puerto_m);

    // Swap inicia la conexión hacia la Memoria 
    int fd_memoria = conectarse_a_modulo(ip_m, puerto_m, logger, HANDSHAKE_SWAP);
    
    if (fd_memoria != -1) {
        // Leer config y enviar tamaños a Memoria
        uint32_t block_size = (uint32_t)config_get_int_value(config, "BLOCK_SIZE");
        uint32_t swap_size = (uint32_t)config_get_int_value(config, "SWAP_FILE_SIZE");
        
        send(fd_memoria, &block_size, sizeof(uint32_t), 0);
        send(fd_memoria, &swap_size, sizeof(uint32_t), 0);

        // Log obligatorio de la cátedra
        log_info(logger, "## Conectado a Kernel Memory");
        
    while(1) {
    int cod_op = recibir_operacion(fd_memoria);
    if (cod_op <= 0) break; // Desconexión

    uint32_t nro_bloque;
    recv(fd_memoria, &nro_bloque, sizeof(uint32_t), MSG_WAITALL);

    if (cod_op == ESCRITURA_BLOQUE) {
        void* datos = malloc(block_size);
        recv(fd_memoria, datos, block_size, MSG_WAITALL);
        
        fseek(archivo_swap, nro_bloque * block_size, SEEK_SET);
        fwrite(datos, block_size, 1, archivo_swap);
        fflush(archivo_swap);
        free(datos);
        
        log_info(logger, "## Escritura del bloque: %u", nro_bloque);
        int ok = 1;
        send(fd_memoria, &ok, sizeof(int), 0); // Confirmar a Memoria
        
    } else if (cod_op == LECTURA_BLOQUE) {
        void* buffer = malloc(block_size);
        fseek(archivo_swap, nro_bloque * block_size, SEEK_SET);
        fread(buffer, block_size, 1, archivo_swap);
        
        log_info(logger, "## Lectura del bloque: %u", nro_bloque);
        send(fd_memoria, buffer, block_size, 0); // Enviar datos a Memoria
        free(buffer);
    }
    }
} else { // Cierra el if (fd_memoria != -1)
        log_error(logger, "No se pudo conectar a la Memoria.");
        return EXIT_FAILURE;
    }
    return EXIT_SUCCESS;
}

void inicializar_archivo_swap(void) {
    char* path = config_get_string_value(config, "SWAP_FILE_PATH");
    uint32_t size = (uint32_t)config_get_int_value(config, "SWAP_FILE_SIZE");

    // "wb+" abre el archivo para lectura/escritura binaria. Si no existe, lo crea. Si existe, lo pisa.
    archivo_swap = fopen(path, "wb+");
    if (archivo_swap == NULL) {
        log_error(logger, "Error grave: No se pudo crear/abrir el archivo SWAP en la ruta: %s", path);
        exit(EXIT_FAILURE);
    }

    // Llenamos el archivo con ceros de una sola vez
    void* buffer_ceros = malloc(size);
    memset(buffer_ceros, 0, size);
    
    fwrite(buffer_ceros, size, 1, archivo_swap);
    fflush(archivo_swap); // Forzamos a que se guarde físicamente en el disco
    free(buffer_ceros);

    log_info(logger, "Archivo SWAP inicializado en %s con %u bytes", path, size);
}