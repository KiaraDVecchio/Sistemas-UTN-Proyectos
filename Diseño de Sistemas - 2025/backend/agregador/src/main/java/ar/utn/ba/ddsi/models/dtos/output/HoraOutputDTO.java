package ar.utn.ba.ddsi.models.dtos.output;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class HoraOutputDTO {
  private LocalTime horaAcontecimiento;
  private Long cantidad;

  public HoraOutputDTO(LocalTime horaAcontecimiento, Long cantidad){
    this.horaAcontecimiento = horaAcontecimiento;
    this.cantidad = cantidad;
  }
}
