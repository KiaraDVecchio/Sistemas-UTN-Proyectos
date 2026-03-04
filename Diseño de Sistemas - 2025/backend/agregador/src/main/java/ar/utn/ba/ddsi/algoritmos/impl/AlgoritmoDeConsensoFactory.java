package ar.utn.ba.ddsi.algoritmos.impl;

import ar.utn.ba.ddsi.algoritmos.IAlgoritmoDeConsenso;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoAlgoritmoDeConsenso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class AlgoritmoDeConsensoFactory {
  private final Map<TipoAlgoritmoDeConsenso, IAlgoritmoDeConsenso> estrategias;

  @Autowired
  public AlgoritmoDeConsensoFactory(List<IAlgoritmoDeConsenso> estrategiasDisponibles) {
    this.estrategias = estrategiasDisponibles.stream()
        .collect(Collectors.toMap(IAlgoritmoDeConsenso::getTipo, Function.identity()));
  }

  public IAlgoritmoDeConsenso resolver(TipoAlgoritmoDeConsenso tipo) {
    return estrategias.get(tipo);
  }
}
