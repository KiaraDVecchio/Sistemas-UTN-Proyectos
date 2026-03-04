package ar.utn.ba.ddsi.Metamapa.services;

import ar.utn.ba.ddsi.Metamapa.exceptions.NotFoundException;
import ar.utn.ba.ddsi.Metamapa.exceptions.ValidationException;
import ar.utn.ba.ddsi.Metamapa.models.dtos.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class ColeccionService {
    private final WebClient webClient;
    private final WebClient webClientPublic;

    public ColeccionService(
            @Qualifier("webClientAdmin") WebClient webClient,
            @Qualifier("webClientPublic") WebClient webClientPublic
    ) {
        this.webClient = webClient;
        this.webClientPublic = webClientPublic;
    }

    public List<ColeccionDTO> getColecciones() {
        // 1) obtener colecciones (salida del backend)
        List<ColeccionDTO> colecciones = webClient.get()
                .uri("/colecciones")
                .retrieve()
                .bodyToFlux(ColeccionDTO.class)
                .collectList()
                .block();

        if (colecciones == null || colecciones.isEmpty()) {
            return List.of();
        }

        return colecciones;
    }


    public PaginaDTO<ColeccionDTO> getColeccionesPaginadas(int page, int size, String keyword) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/colecciones/paginado")
                            .queryParam("page", page)
                            .queryParam("size", size);

                    if (keyword != null && !keyword.isBlank()) {
                        builder.queryParam("keyword", keyword);
                    }
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PaginaDTO<ColeccionDTO>>() {})
                .block();
    }


  public List<CategoriaDTO> getCategorias(){
        List<CategoriaDTO> categorias = webClientPublic.get()
                .uri("/categorias")
                .retrieve()
                .bodyToFlux(CategoriaDTO.class)
                .collectList()
                .block();

        if (categorias == null || categorias.isEmpty()) {
            return List.of();
        }

        return categorias;
    }

    public ColeccionDTO getColeccionByHandle(String handle) {
        try {
            ColeccionDTO coleccion = webClient.get()
                    .uri("/colecciones/{handle}", handle)
                    .retrieve()
                    .bodyToMono(ColeccionDTO.class)
                    .block();

            if (coleccion == null) throw new NoSuchElementException("Colección no encontrada: " + handle);

            return coleccion;
        } catch (WebClientResponseException.NotFound nf) {
            throw new NoSuchElementException("Colección no encontrada: " + handle);
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener la colección: " + e.getMessage(), e);
        }
    }

    public PaginaDTO<HechoDTO> buscarHechos(String handle, String categoria, String fuente,
                                            String ubicacion, String keyword,
                                            LocalDate fechaDesde, LocalDate fechaHasta,
                                            Boolean modoCurado,
                                            Double latitud, Double longitud, Double radio, // <--- Nuevos parámetros
                                            int page, int size) {
        try {
            return webClientPublic.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/colecciones/{handle}/hechos");

                        if (categoria != null && !categoria.isBlank()) {
                            uriBuilder.queryParam("categoria", categoria);
                        }
                        if (fuente != null && !fuente.isBlank()) {
                            uriBuilder.queryParam("fuente", fuente);
                        }
                        if (ubicacion != null && !ubicacion.isBlank()) {
                            uriBuilder.queryParam("ubicacion", ubicacion); // Texto de referencia
                        }
                        if (keyword != null && !keyword.isBlank()) {
                            uriBuilder.queryParam("q", keyword);
                        }
                        if (fechaDesde != null) {
                            uriBuilder.queryParam("fecha_acontecimiento_desde", fechaDesde.toString());
                        }
                        if (fechaHasta != null) {
                            uriBuilder.queryParam("fecha_acontecimiento_hasta", fechaHasta.toString());
                        }

                        if (latitud != null) {
                            uriBuilder.queryParam("latitud", latitud);
                        }
                        if (longitud != null) {
                            uriBuilder.queryParam("longitud", longitud);
                        }
                        if (radio != null) {
                            uriBuilder.queryParam("radio", radio);
                        }


                        if (Boolean.TRUE.equals(modoCurado)) {
                            uriBuilder.queryParam("modo", "CURADO");
                        }

                        uriBuilder.queryParam("page", page);
                        uriBuilder.queryParam("size", size);

                        return uriBuilder.build(handle);
                    })
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<PaginaDTO<HechoDTO>>() {})
                    .block();

        } catch (WebClientResponseException.NotFound nf) {
            PaginaDTO<HechoDTO> vacio = new PaginaDTO<>();
            vacio.setContent(new ArrayList<>());
            vacio.setTotalPages(0);
            vacio.setTotalElements(0L);
            vacio.setNumber(0);
            return vacio;
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar hechos en el agregador: " + e.getMessage(), e);
        }
    }

    public ColeccionDTO crearColeccion(ColeccionDTO coleccion) {
        return webClient.post()
            .uri("/colecciones")
            .bodyValue(coleccion)
            .retrieve()
            .bodyToMono(ColeccionDTO.class)
            .block();
    }

    public void eliminarColeccion(String handle){
        try {
            webClient
                    .delete()
                    .uri("/colecciones/{handle}", handle)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error al eliminar coleccion: " + e.getResponseBodyAsString(), e);
        }
    }

    public void sumarVistaColeccion(String handle) {
        try {
            webClientPublic.post()
                    .uri("/coleccion/{handle}/vista", handle)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

        }catch (WebClientResponseException e){
            throw new RuntimeException("Error al sumar vista a la coleccion: " + e.getResponseBodyAsString(), e);
        }
    }

    public List<ColeccionDTO> traerColeccionesDestacadas(){
        try {
            return webClientPublic.get()
                    .uri("/colecciones-destacadas")
                    .retrieve()
                    .bodyToFlux(ColeccionDTO.class)
                    .collectList()
                    .block();

        }catch (WebClientResponseException e){
            throw new RuntimeException("Error al traer colecciones: " + e.getResponseBodyAsString(), e);
        }
    }

    public ColeccionDTO actualizarColeccion(String handle, ColeccionDTO dto) {
        try {
            return webClient.put()
                .uri("/colecciones/{handle}", handle)   // mapea al AdminController del backend
                .bodyValue(dto)                          // dto con titulo, descripcion, algoritmoDeConsenso, criterioIds, etc.
                .retrieve()
                .bodyToMono(ColeccionDTO.class)
                .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error al actualizar la colección: " + e.getResponseBodyAsString(), e);
        }
    }

    public ColeccionDTO actualizarTodasLasColecciones() {
        try {
            return webClient.put()
                .uri("/colecciones/actualizar")
                .retrieve()
                .bodyToMono(ColeccionDTO.class)
                .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error al actualizar las colecciónes");
        }
    }

}
