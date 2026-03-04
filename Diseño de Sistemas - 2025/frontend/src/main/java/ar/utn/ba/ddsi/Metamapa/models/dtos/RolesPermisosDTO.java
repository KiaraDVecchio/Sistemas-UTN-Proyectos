package ar.utn.ba.ddsi.Metamapa.models.dtos;

import ar.utn.ba.ddsi.Metamapa.enums.Permiso;
import ar.utn.ba.ddsi.Metamapa.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolesPermisosDTO {
    private String username;
    private Rol rol;
    private List<Permiso> permisos;
}