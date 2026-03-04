package ar.utn.ba.ddsi.models.dtos.output;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class HoraOutputDTO {
  private LocalDateTime fechaYHoraAcontecimiento;
  private Long cantidad;

  public HoraOutputDTO(LocalDateTime fechaYHoraAcontecimiento, Long cantidad){
    this.fechaYHoraAcontecimiento = fechaYHoraAcontecimiento;
    this.cantidad = cantidad;
  }
}
