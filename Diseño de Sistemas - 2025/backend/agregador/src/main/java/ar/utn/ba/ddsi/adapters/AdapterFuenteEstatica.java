package ar.utn.ba.ddsi.adapters;

import ar.utn.ba.ddsi.models.dtos.input.HechoInputComunDTO;
import ar.utn.ba.ddsi.models.entities.Categoria;
import ar.utn.ba.ddsi.models.entities.Etiqueta;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.Ubicacion;
import ar.utn.ba.ddsi.models.entities.enumerados.Origen;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class AdapterFuenteEstatica {

  private final WebClient webClient;

@Autowired
  public AdapterFuenteEstatica(WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.build();
  }

  public List<Hecho> obtenerHechos(String fuenteUrl) {
    List<HechoInputComunDTO> hechosDTO = webClient.get()
        .uri(fuenteUrl)
        .retrieve()
        .bodyToFlux(HechoInputComunDTO.class)
        .collectList()
        .block();

    if (hechosDTO == null) return List.of();
    return hechosDTO.stream()
        .map(this::mapToHecho)
        .toList();
  }

  private Hecho mapToHecho(HechoInputComunDTO dto) {
    Hecho hecho = new Hecho(
        dto.getTitulo(),
        dto.getDescripcion(),
        new Categoria(dto.getCategoria()),
        new Ubicacion(
            dto.getUbicacion() != null ? dto.getUbicacion().getLatitud() : null,
            dto.getUbicacion() != null ? dto.getUbicacion().getLongitud() : null, dto.getUbicacion() != null ? dto.getUbicacion().getProvincia() : null
        ),
        dto.getFechaAcontecimiento(),
        dto.getFechaCarga(),
        Origen.PROVENIENTE_DE_DATASET,
        null
    );
    hecho.setFueEliminado(Boolean.TRUE.equals(dto.getFueEliminado()));
    if (dto.getEtiquetas() != null) dto.getEtiquetas().forEach(n -> hecho.agregarEtiqueta(new Etiqueta(n)));

    JsonNode p = dto.getParticulares();
    if (p != null) {
      String rutaNombre = textOrNull(p, "rutaNombre");
      if (rutaNombre != null && !rutaNombre.isBlank()) {
        hecho.setRutaNombre(rutaNombre);
      }
    }
    hecho.setIdEnFuente(dto.getId());

    return hecho;
  }

  private static String textOrNull(JsonNode node, String field) {
    if (node == null) return null;
    JsonNode v = node.path(field);
    return (v.isMissingNode() || v.isNull()) ? null : v.asText();
  }
}