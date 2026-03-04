package ar.utn.ba.ddsi.Metamapa.models.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ContribuyenteDTO {
  private Long id;
  private String nombre;
  private String apellido;
}
