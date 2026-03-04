package ar.utn.ba.ddsi.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GeorefService {

    private final WebClient webClient;

    public GeorefService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://apis.datos.gob.ar/georef/api")
                .build();
    }

    public String obtenerProvincia(Double latitud, Double longitud) {
        if (latitud == null || longitud == null) return null;

        try {
            JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/ubicacion")
                            .queryParam("lat", latitud)
                            .queryParam("lon", longitud)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("ubicacion")) {
                JsonNode ubicacion = response.get("ubicacion");
                if (ubicacion.has("provincia")) {
                    return ubicacion.get("provincia").get("nombre").asText();
                }
            }
        } catch (Exception e) {
            System.err.println("Error consultando Georef: " + e.getMessage());
        }
        return null;
    }
}