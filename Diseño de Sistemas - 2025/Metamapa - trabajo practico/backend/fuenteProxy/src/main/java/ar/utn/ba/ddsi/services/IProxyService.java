package ar.utn.ba.ddsi.services;

import ar.utn.ba.ddsi.models.dtos.input.HechoInputDTO;
import ar.utn.ba.ddsi.models.dtos.output.HechoOutputDTO;

public interface IProxyService {
  HechoOutputDTO hechoOutputDTO(HechoInputDTO hechoInputDTO, String fuenteExterna);
}
