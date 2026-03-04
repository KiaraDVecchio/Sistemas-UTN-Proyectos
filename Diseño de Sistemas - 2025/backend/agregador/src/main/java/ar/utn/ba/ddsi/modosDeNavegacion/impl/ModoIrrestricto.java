package ar.utn.ba.ddsi.modosDeNavegacion.impl;

import ar.utn.ba.ddsi.models.entities.Coleccion;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoAlgoritmoDeConsenso;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoDeModoNavegacion;
import ar.utn.ba.ddsi.modosDeNavegacion.IModoDeNavegacion;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ModoIrrestricto implements IModoDeNavegacion {

  @Override
  public List<Hecho> aplicarModo(List<Hecho> hechos, TipoAlgoritmoDeConsenso algoritmo) {
    return hechos;
  }

  @Override
  public TipoDeModoNavegacion getTipo(){
    return TipoDeModoNavegacion.IRRESTRICTA;
  }
}
