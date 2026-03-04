package ar.utn.ba.ddsi.models.dtos;

import ar.utn.ba.ddsi.models.entities.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContribuyenteDTO {
  private Long id;
  private String nombre;
  private String apellido;

  public ContribuyenteDTO(Usuario userEncontrado) {
    this.id = userEncontrado.getId();
    this.nombre = userEncontrado.getNombre();
    this.apellido = userEncontrado.getApellido();
  }
}
