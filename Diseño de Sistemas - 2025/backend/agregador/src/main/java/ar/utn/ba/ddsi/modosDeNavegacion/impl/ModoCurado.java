package ar.utn.ba.ddsi.modosDeNavegacion.impl;

import ar.utn.ba.ddsi.models.entities.Coleccion;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoAlgoritmoDeConsenso;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoDeModoNavegacion;
import ar.utn.ba.ddsi.modosDeNavegacion.IModoDeNavegacion;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ModoCurado implements IModoDeNavegacion {

  @Override
  public List<Hecho> aplicarModo(List<Hecho> hechos, TipoAlgoritmoDeConsenso algoritmo) {
    return hechos.stream()
        .filter(h -> h.esConsensuado(algoritmo))
        .collect(Collectors.toList());
  }

  @Override
  public TipoDeModoNavegacion getTipo(){
    return TipoDeModoNavegacion.CURADO;
  }
}
