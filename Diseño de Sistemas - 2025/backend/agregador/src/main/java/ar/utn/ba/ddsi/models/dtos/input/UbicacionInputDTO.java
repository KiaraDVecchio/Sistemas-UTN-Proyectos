package ar.utn.ba.ddsi.models.dtos.input;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UbicacionInputDTO {
  private Double latitud;
  private Double longitud;
  private String provincia;
}
