package ar.utn.ba.ddsi.Metamapa.models.dtos;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Setter @Getter
public class RegisterRequestDTO {
    private String nombre;
    private String apellido;
    private String username;
    private String email;
    private LocalDate fechaNacimiento;
    private String contrasenia;
}
