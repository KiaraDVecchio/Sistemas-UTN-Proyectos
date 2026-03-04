package ar.utn.ba.ddsi.adapters;

import ar.utn.ba.ddsi.models.dtos.input.HechoInputDTO;
import java.util.List;

// se define el contrato que van a cumplir todas las fuentes proxy externas
public interface IFuenteProxyAdapter {
  List<HechoInputDTO> getHechos();
}
