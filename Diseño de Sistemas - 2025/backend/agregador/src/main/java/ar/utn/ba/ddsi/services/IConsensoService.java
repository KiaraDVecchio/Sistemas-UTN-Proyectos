package ar.utn.ba.ddsi.services;

import ar.utn.ba.ddsi.models.entities.Coleccion;
import ar.utn.ba.ddsi.models.entities.Fuente;
import ar.utn.ba.ddsi.models.entities.Hecho;
import java.util.List;
import java.util.Map;

public interface IConsensoService {
  void aplicarAlgoritmoDeConsenso();
  void usoDeAlgoritmo(Coleccion coleccion, Map<Fuente, List<Hecho>> hechosPorFuente);
}
