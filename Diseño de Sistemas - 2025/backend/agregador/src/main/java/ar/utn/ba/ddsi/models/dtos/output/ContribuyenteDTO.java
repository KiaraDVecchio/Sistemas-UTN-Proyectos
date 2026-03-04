package ar.utn.ba.ddsi.models.dtos.output;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ContribuyenteDTO {
  private Long id;
  private String nombre;
  private String apellido;
  private LocalDate fechaDeNacimiento;
}
