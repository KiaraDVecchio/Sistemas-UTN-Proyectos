package ar.utn.ba.ddsi.models.dtos.input;

import ar.utn.ba.ddsi.models.entities.Contribuyente;
import ar.utn.ba.ddsi.models.entities.Ubicacion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter
public class HechoInputEdicionDTO {
  @Schema(description = "Título del hecho", example = "Accidente en la ruta 12")
  private String titulo;
  @Schema(description = "Descripcion del hecho", example = "Sucedio en el km 35 de la ruta...")
  private String descripcion;
  //private Integer idCategoria;
  @Schema(description = "Categoria del hecho", example = "Seleccione una Categoria")
  private CategoriaInputDTO categoria;
  @Schema(description = "Ubicacion donde sucedio", example = "Seleccionar ubicacion en el mapa")
  private Ubicacion ciudad;
  @Schema(description = "Fecha en que sucedio", example = "AAAA-MM-DD")
  private LocalDateTime fechaAcontecimiento;
  private Contribuyente contribuyente;
  @Schema(description = "Fotos o videos sobre el hecho", example = "Inserte o arrastre una foto/video")
  private List<String> pathsMultimedia;
}
