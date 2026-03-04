package ar.utn.ba.ddsi.models.dtos.output;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GeoSugerenciaDTO {
  private String label;
  private Double latitud;
  private Double longitud;
  private String provinciaId;
  private String provinciaNombre;
  private String calle;
  private String altura;
  private String localidad;
}
