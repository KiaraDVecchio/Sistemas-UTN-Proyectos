package ar.utn.ba.ddsi.algoritmos;

import ar.utn.ba.ddsi.models.entities.Coleccion;
import ar.utn.ba.ddsi.models.entities.Fuente;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoAlgoritmoDeConsenso;
import java.util.List;
import java.util.Map;

public interface IAlgoritmoDeConsenso {
  void calcularConsenso(Coleccion coleccion, Map<Fuente, List<Hecho>> hechosPorFuente);
  TipoAlgoritmoDeConsenso getTipo();
}
