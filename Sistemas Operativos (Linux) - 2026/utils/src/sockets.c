#include "sockets.h" 
#include <commons/collections/list.h>
#include <stdio.h>


 //Funciones del tp 0

 //ClienteS
void* serializar_paquete(t_paquete* paquete, int bytes)
{
	void * magic = malloc(bytes);
	int desplazamiento = 0;

	memcpy(magic + desplazamiento, &(paquete->codigo_operacion), sizeof(int));
	desplazamiento+= sizeof(int);
	memcpy(magic + desplazamiento, &(paquete->buffer->size), sizeof(int));
	desplazamiento+= sizeof(int);
	memcpy(magic + desplazamiento, paquete->buffer->stream, paquete->buffer->size);
	desplazamiento+= paquete->buffer->size;

	return magic;
}

int crear_conexion(char *ip, char* puerto)
{
	struct addrinfo hints;
	struct addrinfo *server_info;

	memset(&hints, 0, sizeof(hints));
	hints.ai_family = AF_INET;
	hints.ai_socktype = SOCK_STREAM;
	hints.ai_flags = AI_PASSIVE;

	if (getaddrinfo(ip, puerto, &hints, &server_info) != 0) {
		return -1;
	}

	// Ahora vamos a crear el socket.
	int socket_cliente = socket(server_info->ai_family, server_info->ai_socktype, server_info->ai_protocol);
	if (socket_cliente == -1) {
		freeaddrinfo(server_info);
		return -1;
	}

	// Ahora que tenemos el socket, vamos a conectarlo
	if (connect(socket_cliente, server_info->ai_addr, server_info->ai_addrlen) == -1) {
		close(socket_cliente);
		freeaddrinfo(server_info);
		return -1;
	}

	freeaddrinfo(server_info);

	return socket_cliente;
}

void enviar_mensaje(char* mensaje, int socket_cliente)
{
	t_paquete* paquete = malloc(sizeof(t_paquete));

	paquete->codigo_operacion = MENSAJE;
	paquete->buffer = malloc(sizeof(t_buffer));
	paquete->buffer->size = strlen(mensaje) + 1;
	paquete->buffer->stream = malloc(paquete->buffer->size);
	memcpy(paquete->buffer->stream, mensaje, paquete->buffer->size);

	int bytes = paquete->buffer->size + 2*sizeof(int);

	void* a_enviar = serializar_paquete(paquete, bytes);

	send(socket_cliente, a_enviar, bytes, 0);

	free(a_enviar);
	eliminar_paquete(paquete);
}


void crear_buffer(t_paquete* paquete)
{
	paquete->buffer = malloc(sizeof(t_buffer));
	paquete->buffer->size = 0;
	paquete->buffer->stream = NULL;
}

t_paquete* crear_paquete(void)
{
	t_paquete* paquete = malloc(sizeof(t_paquete));
	paquete->codigo_operacion = PAQUETE;
	crear_buffer(paquete);
	return paquete;
}

void agregar_a_paquete(t_paquete* paquete, void* valor, int tamanio)
{
	paquete->buffer->stream = realloc(paquete->buffer->stream, paquete->buffer->size + tamanio + sizeof(int));

	memcpy(paquete->buffer->stream + paquete->buffer->size, &tamanio, sizeof(int));
	memcpy(paquete->buffer->stream + paquete->buffer->size + sizeof(int), valor, tamanio);

	paquete->buffer->size += tamanio + sizeof(int);
}

void enviar_paquete(t_paquete* paquete, int socket_cliente)
{
	int bytes = paquete->buffer->size + 2*sizeof(int);
	void* a_enviar = serializar_paquete(paquete, bytes);

	send(socket_cliente, a_enviar, bytes, 0);

	free(a_enviar);
}

void eliminar_paquete(t_paquete* paquete)
{
	free(paquete->buffer->stream);
	free(paquete->buffer);
	free(paquete);
}

void liberar_conexion(int socket_cliente)
{
	close(socket_cliente);
}

//Servidor

int iniciar_servidor(char* puerto) { //ya no lo harcodeamos
    struct addrinfo hints, *server_info;
    memset(&hints, 0, sizeof(hints));
    hints.ai_family   = AF_INET;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags    = AI_PASSIVE;

    getaddrinfo(NULL, puerto, &hints, &server_info);

    int socket_servidor = socket(server_info->ai_family,
                                 server_info->ai_socktype,
                                 server_info->ai_protocol);

    //Evitamos "Address already in use" al reiniciar  -> o sea evita el error de que el puerto se haya quedado escuchando si se cierra el programa de golpe
    int reuseaddr = 1;
    setsockopt(socket_servidor, SOL_SOCKET, SO_REUSEADDR, &reuseaddr, sizeof(reuseaddr));

    bind(socket_servidor, server_info->ai_addr, server_info->ai_addrlen);
    listen(socket_servidor, SOMAXCONN);

    freeaddrinfo(server_info);
    return socket_servidor;
}

int esperar_cliente(int socket_servidor) {
    struct sockaddr_in dir_cliente;
    socklen_t tam_dir = sizeof(struct sockaddr_in);
    int socket_cliente = accept(socket_servidor,(struct sockaddr*) &dir_cliente,&tam_dir); //guardamos la dirección del cliente y su tamaño a diferencia del tp0
    return socket_cliente;
}

//Recibimos
int recibir_operacion(int socket_cliente) {
    int cod_op;
    if (recv(socket_cliente, &cod_op, sizeof(int), MSG_WAITALL) > 0)
        return cod_op;
    return -1;  
}

char* recibir_mensaje(int socket_cliente) {
    int size;
    recv(socket_cliente, &size, sizeof(int), MSG_WAITALL);
    char* mensaje = malloc(size);
    recv(socket_cliente, mensaje, size, MSG_WAITALL);
    return mensaje;
    //Recordar hacer el free cuando usemos recibir_mensaje en el main.c
}

t_list* recibir_paquete(int socket_cliente) {
    int size;
    recv(socket_cliente, &size, sizeof(int), MSG_WAITALL);
    void* stream = malloc(size);
    recv(socket_cliente, stream, size, MSG_WAITALL);

    t_list* lista = list_create();
    int desplazamiento = 0;
    while (desplazamiento < size) {
        int tam_valor;
        memcpy(&tam_valor, stream + desplazamiento, sizeof(int));
        desplazamiento += sizeof(int);
        char* valor = malloc(tam_valor);
        memcpy(valor, stream + desplazamiento, tam_valor);
        desplazamiento += tam_valor;
        list_add(lista, valor);
    }
    free(stream);
    return lista;
}


void enviar_contexto(int socket_destino, int op_code, t_contexto_proceso* contexto) {
    int cant_segmentos = list_size(contexto->tabla_segmentos);
    
    // Calculamos tamaño: PID + Registros + Cantidad de Segmentos + (Segmentos * tamaño de 1 segmento)
    int size_payload = sizeof(uint32_t) + sizeof(t_registros) + sizeof(int) + (cant_segmentos * sizeof(t_segmento));
    void* buffer = malloc(size_payload);
    int offset = 0;
    
    memcpy(buffer + offset, &(contexto->pid), sizeof(uint32_t));
    offset += sizeof(uint32_t);
    
    memcpy(buffer + offset, &(contexto->registros), sizeof(t_registros));
    offset += sizeof(t_registros);
    
    memcpy(buffer + offset, &cant_segmentos, sizeof(int));
    offset += sizeof(int);
    
    // Serializamos la lista de segmentos
    for(int i = 0; i < cant_segmentos; i++) {
        t_segmento* seg = list_get(contexto->tabla_segmentos, i);
        memcpy(buffer + offset, seg, sizeof(t_segmento));
        offset += sizeof(t_segmento);
    }
    
    send(socket_destino, &op_code, sizeof(int), 0);
    send(socket_destino, &size_payload, sizeof(int), 0);
    send(socket_destino, buffer, size_payload, 0);
    
    free(buffer);
}

t_contexto_proceso* recibir_contexto(int socket_origen) {
    t_contexto_proceso* contexto_nuevo = malloc(sizeof(t_contexto_proceso));
    contexto_nuevo->tabla_segmentos = list_create();
    
    int size_payload;
    if (recv(socket_origen, &size_payload, sizeof(int), MSG_WAITALL) <= 0) {
        contexto_nuevo->pid = -1;
        return contexto_nuevo;
    }
    
    void* buffer = malloc(size_payload);
    recv(socket_origen, buffer, size_payload, MSG_WAITALL);
    
    int offset = 0;
    memcpy(&(contexto_nuevo->pid), buffer + offset, sizeof(uint32_t));
    offset += sizeof(uint32_t);
    
    memcpy(&(contexto_nuevo->registros), buffer + offset, sizeof(t_registros));
    offset += sizeof(t_registros);
    
    int cant_segmentos;
    memcpy(&cant_segmentos, buffer + offset, sizeof(int));
    offset += sizeof(int);
    
    // Deserializamos la lista de segmentos
    for(int i = 0; i < cant_segmentos; i++) {
        t_segmento* seg = malloc(sizeof(t_segmento));
        memcpy(seg, buffer + offset, sizeof(t_segmento));
        offset += sizeof(t_segmento);
        list_add(contexto_nuevo->tabla_segmentos, seg);
    }
    
    free(buffer);
    return contexto_nuevo;
}

t_contexto_proceso* pedir_contexto(int fd, uint32_t pid) {
    int op = SOLICITUD_CONTEXTO;
    send(fd, &op, sizeof(int), 0);
    send(fd, &pid, sizeof(uint32_t), 0);
    
    int op_code = recibir_operacion(fd);
    if (op_code == CONTEXTO_RESPUESTA) {
        t_contexto_proceso* contexto_recibido = recibir_contexto(fd);
 if (contexto_recibido->pid != pid) {
            printf("DESINCRONIZACIÓN: Pedí contexto %u y llegó %u\n", pid, contexto_recibido->pid);
        }
        return contexto_recibido;
    } else {
        printf("Error al pedir contexto. OpCode: %d\n", op_code);
        t_contexto_proceso* ctx_vacio = malloc(sizeof(t_contexto_proceso));
        ctx_vacio->pid = -1;
        ctx_vacio->tabla_segmentos = list_create();
        return ctx_vacio;
    }

}