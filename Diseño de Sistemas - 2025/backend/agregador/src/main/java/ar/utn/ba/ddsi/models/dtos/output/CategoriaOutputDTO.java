package ar.utn.ba.ddsi.models.dtos.output;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoriaOutputDTO {
    private Long id;
  private String nombre;

  public CategoriaOutputDTO(String nombre){
    this.nombre = nombre;
  }

}
