package ar.utn.ba.ddsi.models.dtos.output;

import ar.utn.ba.ddsi.models.entities.Ruta;
import ar.utn.ba.ddsi.models.entities.enumerados.Origen;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class HechoOutputEstaticaDTO {
  @NotNull
  private Long id;
  @NotNull
  private String titulo;
  @NotNull
  private String descripcion;
  @NotNull
  private String categoria;
  @NotNull
  private UbicacionOutputDTO ubicacion;
  @NotNull
  private LocalDateTime fechaAcontecimiento;
  @NotNull
  private LocalDate fechaCarga;
  @NotNull
  private boolean fueEliminado;
  @NotNull
  private Set<String> etiquetas;

  private JsonNode particulares;
}
