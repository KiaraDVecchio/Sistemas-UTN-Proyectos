package ar.utn.ba.ddsi.Metamapa.services;

import ar.utn.ba.ddsi.Metamapa.models.dtos.SolicitudDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.SolicitudEdicionCreacionDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class SolicitudService {
    private final WebClient webClientPublic;
    private final WebClient webClientAdmin;
    private final WebClient webClientDin;

    public SolicitudService(WebClient.Builder webClientBuilder,
                            @Value("${backend.api.base-url-agregador}") String baseUrl,
                            @Value("${backend.api.base-url}") String baseUrlAdmin,
                            @Value("${backend.api.base-url-dinamica}") String backendDinamica) {
        this.webClientPublic = webClientBuilder
                .baseUrl(baseUrl)
                .build();
        this.webClientAdmin = webClientBuilder
                .baseUrl(baseUrlAdmin)
                .build();
        this.webClientDin = webClientBuilder
                .baseUrl(backendDinamica)
                .build();
    }

    public List<SolicitudDTO> getSolicitudes() {
        List<SolicitudDTO> solicitudes = webClientPublic.get()
                .uri("/solicitudes")
                .retrieve()
                .bodyToFlux(SolicitudDTO.class)
                .collectList()
                .block();

        if (solicitudes == null || solicitudes.isEmpty()) {
            return List.of();
        }

        return solicitudes;
    }

    public SolicitudDTO crearSolicitudEliminacion(Long hechoId, String justificacion) {
        try {

            return webClientPublic.post()
                    .uri("/solicitudes")
                    .bodyValue(Map.of("hechoId", hechoId, "justificacion", justificacion))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            clientResponse.bodyToMono(String.class)
                                    .flatMap(body -> Mono.<RuntimeException>error(
                                            new RuntimeException("Backend error: " + body)
                                    ))
                    )
                    .bodyToMono(SolicitudDTO.class)
                    .block();

        } catch (Exception e) {
            throw new RuntimeException("No se pudo crear la solicitud: " + e.getMessage(), e);
        }
    }

    public void aprobarSolicitud(Long id){
        try{
            webClientAdmin.post()
                    .uri("/solicitudes-eliminacion/{id}/aprobar", id)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        }catch(WebClientResponseException e){
            throw new RuntimeException("Error al aprobar solicitud: " + e.getResponseBodyAsString(), e);
        }
    }

    public void rechazarSolicitud(Long id){
        try{
            webClientAdmin.post()
                    .uri("/solicitudes-eliminacion/{id}/denegar", id)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        }catch(WebClientResponseException e){
            throw new RuntimeException("Error al rechazar solicitud: " + e.getResponseBodyAsString(), e);
        }
    }

    public SolicitudDTO obtenerSolicitud(Long id){
        try{
            return webClientAdmin.get()
                    .uri("/solicitudes/{id}", id)
                    .retrieve()
                    .bodyToMono(SolicitudDTO.class)
                    .block();
        }catch(WebClientResponseException e){
            throw new RuntimeException("Error al traer solicitud: " + e.getResponseBodyAsString(), e);
        }
    }

    public List<SolicitudEdicionCreacionDTO> getSolicitudesEdicionCreacion() {
        String urlDinamica = "/solicitudes";

        List<SolicitudEdicionCreacionDTO> solicitudes = webClientDin.get()
                .uri(urlDinamica)
                .retrieve()
                .bodyToFlux(SolicitudEdicionCreacionDTO.class)
                .collectList()
                .block();

        if (solicitudes == null) return List.of();

        return solicitudes;
    }

    public void aceptarSolicitudEdicionCreacion(Long idSolicitud) {
        String url = "/solicitudes/{id}/aceptar";

        try {
            webClientDin.post()
                    .uri(url, idSolicitud)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error al aceptar solicitud en Dinámica: " + e.getResponseBodyAsString(), e);
        }
    }

    public void rechazarSolicitudEdicionCreacion(Long idSolicitud) {
        String url = "/solicitudes/{id}/rechazar";

        try {
            webClientDin.post()
                    .uri(url, idSolicitud)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error al rechazar solicitud en Dinámica: " + e.getResponseBodyAsString(), e);
        }
    }
}