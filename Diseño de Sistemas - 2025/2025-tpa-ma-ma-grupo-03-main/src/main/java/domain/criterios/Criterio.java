package domain.criterios;

import domain.Hecho;

public interface Criterio {

  public abstract boolean cumpleCriterio(Hecho hecho);
}
