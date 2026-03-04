package ar.utn.ba.ddsi.modosDeNavegacion.impl;

import ar.utn.ba.ddsi.models.entities.enumerados.TipoDeModoNavegacion;
import ar.utn.ba.ddsi.modosDeNavegacion.IModoDeNavegacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ModoDeNavegacionFactory {
  private final Map<TipoDeModoNavegacion, IModoDeNavegacion> modos;

  @Autowired
  public ModoDeNavegacionFactory(List<IModoDeNavegacion> modosDisponibles) {
    this.modos = modosDisponibles.stream()
        .collect(Collectors.toMap(IModoDeNavegacion::getTipo, Function.identity()));
  }

  public IModoDeNavegacion resolver(TipoDeModoNavegacion tipo) {
    return modos.get(tipo);
  }
}
