package ar.utn.ba.ddsi.models.dtos.input;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Tiene que contener id o nombre (no los dos)")
public class CategoriaInputDTO {
  @Schema(description = "ID de la categoría")
  private Long id;
  @Schema(description = "Nombre de la categoría")
  private String nombre;
}
