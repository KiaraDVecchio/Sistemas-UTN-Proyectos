package ar.utn.ba.ddsi.models.dtos.output;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoriaOutputDTO {
  private String nombre;
  private Long cantidad;

  public CategoriaOutputDTO(String nombre, Long cantidad){
    this.nombre = nombre;
    this.cantidad = cantidad;
  }

}
