package ar.utn.ba.ddsi.models.repositories.impl;

import ar.utn.ba.ddsi.models.entities.Categoria;
import ar.utn.ba.ddsi.models.entities.ContenidoMultimedia;
import ar.utn.ba.ddsi.models.repositories.IContenidoMultimediaRepository;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
/*
@Repository
public class ContenidoMultimediaRepository implements IContenidoMultimediaRepository {

  private List<ContenidoMultimedia> contenidosMultimedia = new ArrayList<>();

  @Override
  public ContenidoMultimedia findById(Long id) {
    return contenidosMultimedia
        .stream()
        .filter(c -> c.getIdContenidoMultimedia().equals(id))
        .findFirst()
        .orElse(null);
  }

  public Long save(ContenidoMultimedia contenidoMultimedia) {
    contenidoMultimedia.setIdContenidoMultimedia(generarNuevoId());
    this.contenidosMultimedia.add(contenidoMultimedia);
    return contenidoMultimedia.getIdContenidoMultimedia();
  }

  @Override
  public Long generarNuevoId() {
    return contenidosMultimedia.stream()
        .mapToLong(ContenidoMultimedia::getIdContenidoMultimedia)
        .max()
        .orElse(0L) + 1; // si la lista está vacía (O de valor Long), empezamos desde ID 1
  }

  @Override
  public void delete(Long idContenidoMultimedia){
    contenidosMultimedia.removeIf(contenido -> contenido.getIdContenidoMultimedia().equals(idContenidoMultimedia));
  }
}
*/