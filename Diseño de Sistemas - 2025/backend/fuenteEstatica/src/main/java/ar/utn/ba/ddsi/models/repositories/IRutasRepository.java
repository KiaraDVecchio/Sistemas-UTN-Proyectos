package ar.utn.ba.ddsi.models.repositories;

import ar.utn.ba.ddsi.models.entities.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IRutasRepository extends JpaRepository<Ruta,Long> {
  /*void save(Ruta ruta);
  Ruta findById(Long id);
  Long generarNuevoId();
  List<Ruta> findAll();*/
}
