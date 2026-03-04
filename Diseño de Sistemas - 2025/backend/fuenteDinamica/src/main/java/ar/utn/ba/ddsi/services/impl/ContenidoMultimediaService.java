package ar.utn.ba.ddsi.services.impl;

import ar.utn.ba.ddsi.models.dtos.input.HechoInputDTO;
import ar.utn.ba.ddsi.models.entities.ContenidoMultimedia;
import ar.utn.ba.ddsi.models.repositories.IContenidoMultimediaRepository;
import ar.utn.ba.ddsi.services.IContenidoMultimediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContenidoMultimediaService implements IContenidoMultimediaService {

  @Autowired
  private IContenidoMultimediaRepository contenidoMultimediaRepository;

  @Override
  public List<ContenidoMultimedia> mapeosMultimedia(List<String> paths) {
    return paths.stream()
        .map(path -> {
          ContenidoMultimedia nuevoContenido = new ContenidoMultimedia();
          nuevoContenido.setPath(path);
          nuevoContenido.setIdContenidoMultimedia(contenidoMultimediaRepository.save(nuevoContenido).getIdContenidoMultimedia());
          return nuevoContenido;
        })
        .collect(Collectors.toList());
  }
}
