package ar.utn.ba.ddsi.models.entities;

import ar.utn.ba.ddsi.models.dtos.input.HechoInputDTO;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoFuente;
import java.util.List;

public abstract class Fuente {
  public abstract List<Hecho> getHechos();
  public abstract TipoFuente getTipo();
}
