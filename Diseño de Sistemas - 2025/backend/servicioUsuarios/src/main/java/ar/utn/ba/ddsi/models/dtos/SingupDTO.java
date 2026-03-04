package ar.utn.ba.ddsi.models.dtos;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class SingupDTO {
  private String nombre;
  private String apellido;
  private String username;
  private String email;
  private LocalDate fechaNacimiento;
  private String contrasenia;

}
