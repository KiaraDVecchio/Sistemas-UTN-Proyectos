package domain.criterios;

import domain.Hecho;

public class CriterioTitulo implements Criterio{
  private String titulo;

  public CriterioTitulo(String titulo) {
    this.titulo = titulo;
  }

  @Override
  public boolean cumpleCriterio(Hecho hecho){

    return titulo.equalsIgnoreCase(hecho.getTitulo());
  }
}
