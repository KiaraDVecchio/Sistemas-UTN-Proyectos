package ar.utn.ba.ddsi.models.dtos.input;

import ar.utn.ba.ddsi.models.entities.Categoria;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.Ubicacion;
import ar.utn.ba.ddsi.models.entities.enumerados.Origen;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import java.time.LocalDate;
import java.time.OffsetDateTime;


@Getter
@Data
public class HechoInputDTO {
  private long id;
  private String titulo;
  private String descripcion;
  private String categoria;
  private Double latitud;
  private Double longitud;

  @JsonProperty("fecha_hecho")
  private OffsetDateTime fechaAcontecimiento;

  @JsonProperty("created_at")
  private OffsetDateTime fechaCarga;

  private String fuenteExterna;
}



