package ar.utn.ba.ddsi.models.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InformeDeResultados {
  private String nombreOriginal;
  private String guardadoComo;
  private long hechosTotales;
  private long guardadosTotales;
  private long tiempoTardado;
}