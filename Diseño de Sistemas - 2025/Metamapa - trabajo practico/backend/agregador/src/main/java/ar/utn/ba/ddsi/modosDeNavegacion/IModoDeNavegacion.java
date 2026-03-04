package ar.utn.ba.ddsi.modosDeNavegacion;

import ar.utn.ba.ddsi.models.entities.Coleccion;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoAlgoritmoDeConsenso;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoDeModoNavegacion;
import java.util.List;

public interface IModoDeNavegacion {
  List<Hecho> aplicarModo(List<Hecho> hechos, TipoAlgoritmoDeConsenso algoritmo);
  TipoDeModoNavegacion getTipo();
}