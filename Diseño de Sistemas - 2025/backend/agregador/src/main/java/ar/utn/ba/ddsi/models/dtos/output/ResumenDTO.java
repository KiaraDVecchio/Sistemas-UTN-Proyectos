package ar.utn.ba.ddsi.models.dtos.output;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ResumenDTO {
  public Long totalHechos;
  public Long totalFuentes;
  public Long solicitudesPendientes;
}
