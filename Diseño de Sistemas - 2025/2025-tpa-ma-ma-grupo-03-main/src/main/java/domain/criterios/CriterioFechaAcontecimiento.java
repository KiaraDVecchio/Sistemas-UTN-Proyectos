package domain.criterios;

import domain.Hecho;
import java.time.LocalDate;

public class CriterioFechaAcontecimiento implements Criterio{
  private LocalDate desde;
  private LocalDate hasta;

  public CriterioFechaAcontecimiento(LocalDate desde, LocalDate hasta){
    this.desde = desde;
    this.hasta = hasta;
  }


  @Override
  public boolean cumpleCriterio(Hecho hecho) {

    return hecho.getFechaAcontecimiento().isAfter(desde) && hecho.getFechaAcontecimiento().isBefore(hasta);
  }
}
