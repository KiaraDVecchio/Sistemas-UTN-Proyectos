package ar.utn.ba.ddsi.models.repositories;

import ar.utn.ba.ddsi.models.entities.Categoria;
import ar.utn.ba.ddsi.models.entities.ContenidoMultimedia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IContenidoMultimediaRepository extends JpaRepository<ContenidoMultimedia,Long> {

/*  ContenidoMultimedia findById(Long id);
  Long save(ContenidoMultimedia contenidoMultimedia);
  Long generarNuevoId();
  void delete(Long idContenidoMultimedia);

 */
}
