package domain.criterios;

import domain.Hecho;

public class CriterioDescripcion implements Criterio{
    private String descripcion;

  public CriterioDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  @Override
  public boolean cumpleCriterio(Hecho hecho){

    return descripcion.equalsIgnoreCase(hecho.getDescripcion());
  }
}
