package domain.criterios;

import domain.Categoria;
import domain.Hecho;

public class CriterioCategoria implements Criterio{
  private Categoria categoria;

  public CriterioCategoria(Categoria categoria){
    this.categoria = categoria;
  }

  @Override
  public boolean cumpleCriterio(Hecho hecho) {

    return categoria.getNombre().equalsIgnoreCase(hecho.getCategoria().getNombre());
  }

}
