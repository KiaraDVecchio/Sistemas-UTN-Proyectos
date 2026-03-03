package domain.criterios;

import domain.Hecho;
import domain.enumerados.Origen;

public class CriterioOrigen implements Criterio{
  private Origen origen;

  public CriterioOrigen(Origen origen) {
    this.origen = origen;
  }

  @Override
  public boolean cumpleCriterio(Hecho hecho) {

    return origen.equals(hecho.getOrigen());
  }
}
