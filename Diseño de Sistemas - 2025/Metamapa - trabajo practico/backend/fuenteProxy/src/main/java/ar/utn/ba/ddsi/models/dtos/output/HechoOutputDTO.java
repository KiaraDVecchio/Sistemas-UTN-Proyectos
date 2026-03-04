package ar.utn.ba.ddsi.models.dtos.output;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class HechoOutputDTO {
  private String titulo;
  private String descripcion;
  private String categoria;
    private Double latitud;
    private Double longitud;
  private LocalDate fechaAcontecimiento;
  private LocalDate fechaCarga;
    private String fuenteExterna;
}
