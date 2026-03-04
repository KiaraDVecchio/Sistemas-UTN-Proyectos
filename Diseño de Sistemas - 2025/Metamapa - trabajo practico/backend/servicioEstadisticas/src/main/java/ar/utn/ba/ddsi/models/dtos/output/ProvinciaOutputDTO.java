package ar.utn.ba.ddsi.models.dtos.output;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProvinciaOutputDTO {
  private String provincia;
  private int cantidad;

  public ProvinciaOutputDTO(String provincia , int cantidad) {
    this.provincia = provincia;
    this.cantidad = cantidad;
  }
}
