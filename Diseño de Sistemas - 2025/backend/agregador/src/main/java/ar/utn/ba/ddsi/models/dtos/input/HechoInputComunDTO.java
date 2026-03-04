package ar.utn.ba.ddsi.models.dtos.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HechoInputComunDTO {
  private Long id;
  private String titulo;
  private String descripcion;
  private String categoria;
  private UbicacionInputDTO ubicacion;
  private LocalDateTime fechaAcontecimiento;
  private LocalDate fechaCarga;
  private String fuente;
  private Boolean fueEliminado;
  private Boolean consensuado;

  // opcionales comunes
  private Set<String> etiquetas;
  private List<String> pathContenidoMultimedia;

  // Campos especificos de cada fuente
  private JsonNode particulares;
    private boolean tieneEdicionPendiente;
}
