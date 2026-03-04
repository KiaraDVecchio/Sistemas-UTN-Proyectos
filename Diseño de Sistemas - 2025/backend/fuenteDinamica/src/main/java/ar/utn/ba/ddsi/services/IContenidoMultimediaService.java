package ar.utn.ba.ddsi.services;

import ar.utn.ba.ddsi.models.dtos.input.HechoInputDTO;
import ar.utn.ba.ddsi.models.entities.ContenidoMultimedia;
import java.util.List;

public interface IContenidoMultimediaService {
  List<ContenidoMultimedia> mapeosMultimedia(List<String> paths);
}
