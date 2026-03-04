package ar.utn.ba.ddsi.Metamapa.services;

import ar.utn.ba.ddsi.Metamapa.models.dtos.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.util.List;

@Service
public class AdminService {

  private final WebClient webClientPublic;
  private final WebClient webClientAdmin;
    private final WebClient webClientEstadisticas;

  public AdminService(WebClient.Builder webClientBuilder,
                      @Value("${backend.api.base-url-agregador}") String baseUrl,
                      @Value("${backend.api.base-url}") String baseUrlAdmin,
                      @Value("${backend.api.base-url-estadisticas}") String baseUrlEstadisticas,
                      ObjectMapper objectMapper) {
    this.webClientPublic = webClientBuilder
        .baseUrl(baseUrl)
        .build();
    this.webClientAdmin = webClientBuilder
        .baseUrl(baseUrlAdmin)
        .build();
    this.webClientEstadisticas = webClientBuilder
            .baseUrl(baseUrlEstadisticas)
            .build();
  }

    public PaginaDTO<HechoDTO> obtenerHechosPaginado(
            int page, int size,
            Long id, String ubicacion, String estado, LocalDate fecha,
            // NUEVOS
            Double lat, Double lon, Double radio
    ) {
        return webClientPublic.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/paginado")
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .queryParam("sort", "fechaCarga,desc");

                    if (id != null) builder.queryParam("idHecho", id);
                    if (ubicacion != null && !ubicacion.isBlank()) builder.queryParam("ubicacion", ubicacion);
                    if (estado != null && !estado.isBlank()) builder.queryParam("estado", estado);
                    if (fecha != null) builder.queryParam("fecha", fecha);

                    return builder.build();
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PaginaDTO<HechoDTO>>() {})
                .block();
    }

  public List<CategoriaDTO> obtenerCategorias() {
      return webClientPublic.get()
              .uri("/categorias")
              .retrieve()
              .bodyToMono(new ParameterizedTypeReference<List<CategoriaDTO>>() {})
              .block();
  }

    public void eliminarCategoria(Long id){
        try {
            webClientAdmin
                    .delete()
                    .uri("/categorias/{id}", id)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error al eliminar categoria: " + e.getResponseBodyAsString(), e);
        }
    }

    public CategoriaDTO actualizarCategoria(Long id, CategoriaDTO categoria) {
        try {
            return webClientAdmin
                    .put()
                    .uri("/categorias/{id}", id)
                    .bodyValue(categoria)
                    .retrieve()
                    .bodyToMono(CategoriaDTO.class)
                    .block();
        } catch (WebClientResponseException e) {

            throw new RuntimeException("Error al actualizar categoria: " + e.getResponseBodyAsString(), e);
        }
    }
  public InformeDeResultadosDTO importarHechosCsv(MultipartFile archivo) {
    MultipartBodyBuilder body = new MultipartBodyBuilder();
    body.part("archivo", archivo.getResource())
        .filename(archivo.getOriginalFilename() != null ? archivo.getOriginalFilename() : "import.csv")
        .contentType(MediaType.parseMediaType("text/csv"));

    return webClientAdmin.post()
        .uri("/import/hechos/csv")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(BodyInserters.fromMultipartData(body.build()))
        .retrieve()
        .bodyToMono(InformeDeResultadosDTO.class)
        .block();
  }

    public CategoriaDTO crearCategoria(CategoriaDTO categoria) {
        return webClientAdmin
                .post()
                .uri("/categorias")
                .bodyValue(categoria)
                .retrieve()
                .bodyToMono(CategoriaDTO.class)
                .block();
    }

// En AdminService.java

    public DashboardDTO obtenerEstadisticas(String rango) {
        try {
            return webClientEstadisticas.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/estadisticas/dashboard")
                            .queryParam("rango", rango)
                            .build())
                    .retrieve()
                    .bodyToMono(DashboardDTO.class)
                    .block();
        } catch (Exception e) {
            System.err.println("Error obteniendo estadísticas: " + e.getMessage());

            // --- CORRECCIÓN: Fallback seguro ---
            DashboardDTO fallback = new DashboardDTO();
            fallback.setTotalHechos(0L);
            fallback.setHechosVerificados(0L);
            fallback.setSpamDetectado(0L);
            fallback.setPorcentajeSpam(0.0);


            fallback.setHechosPorCategoria(java.util.Collections.emptyMap());
            fallback.setHechosPorProvincia(java.util.Collections.emptyMap());
            fallback.setHechosPorHora(java.util.Collections.emptyMap());

            fallback.setDetallesPorCategoria(java.util.Collections.emptyList());

            return fallback;
        }
    }
    public ByteArrayResource exportarCsv() {
        try {
            byte[] bytes = webClientEstadisticas.get()
                    .uri("/estadisticas/exportar")
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            return new ByteArrayResource(bytes != null ? bytes : new byte[0]);
        } catch (Exception e) {
            throw new RuntimeException("Error al descargar CSV: " + e.getMessage());
        }
    }


    public PaginaDTO<CategoriaDTO> obtenerCategoriasPaginado(int page, int size) {

        return webClientPublic.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/categorias/paginado")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<PaginaDTO<CategoriaDTO>>() {})
                .block();
    }


// En AdminService.java (Frontend)

    public PaginaDTO<SolicitudDTO> obtenerSolicitudesPaginado(
            int page, int size,
            Long id, String estado, LocalDate fecha
    ) {
        return webClientAdmin.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/solicitudes/paginado")
                            .queryParam("page", page)
                            .queryParam("size", size);

                    if (id != null) builder.queryParam("id", id);
                    if (estado != null && !estado.isBlank()) builder.queryParam("estado", estado);
                    if (fecha != null) builder.queryParam("fecha", fecha);

                    return builder.build();
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<PaginaDTO<SolicitudDTO>>() {})
                .block();
    }

}
