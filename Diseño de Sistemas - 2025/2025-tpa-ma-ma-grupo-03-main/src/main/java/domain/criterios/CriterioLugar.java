
package domain.criterios;

import domain.Hecho;
import domain.Ubicacion;

public class CriterioLugar implements Criterio {
  private Ubicacion ubicacion;
  private int rangoMaximo; // num de tolerancia maxima

  public CriterioLugar(Ubicacion ubicacion, int rangoMaximo) {
    this.ubicacion = ubicacion;
    this.rangoMaximo = rangoMaximo;
  }

  @Override
  public boolean cumpleCriterio(Hecho hecho) {
    double distanciaLatitud = Math.abs(hecho.getUbicacion().getLatitud() - ubicacion.getLatitud());
    double distanciaLongitud = Math.abs(hecho.getUbicacion().getLongitud() - ubicacion.getLongitud());
    double distancia = distanciaLatitud + distanciaLongitud;

    return distancia <= rangoMaximo;
  }
}