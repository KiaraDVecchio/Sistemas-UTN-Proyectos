package ar.utn.ba.ddsi.Metamapa.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InformeDeResultadosDTO {
  private String nombreOriginal;
  private String guardadoComo;
  private long hechosTotales;
  private long guardadosTotales;
  private long tiempoTardado;
}
