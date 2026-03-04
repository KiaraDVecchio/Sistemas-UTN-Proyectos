package ar.utn.ba.ddsi.models.dtos.output;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProvinciaOutputDTO {
  private String provincia;
  private Long cantidad;

public ProvinciaOutputDTO(String provincia, Long cantidad){
  this.provincia = provincia;
  this.cantidad = cantidad;
}
}
