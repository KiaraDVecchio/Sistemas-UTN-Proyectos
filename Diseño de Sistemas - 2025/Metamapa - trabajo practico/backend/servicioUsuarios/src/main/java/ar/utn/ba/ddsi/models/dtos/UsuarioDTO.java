package ar.utn.ba.ddsi.models.dtos;

import ar.utn.ba.ddsi.models.enums.Permiso;
import ar.utn.ba.ddsi.models.enums.Rol;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String nombreDeUsuario;
    private String contrasenia; // cifrada
    private Rol rol;
    private Set<Permiso> permisos = new HashSet<>();
}
