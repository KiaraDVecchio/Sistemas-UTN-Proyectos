package ar.utn.ba.ddsi.models.repositories;

import ar.utn.ba.ddsi.models.entities.Hecho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IHechoRepository extends JpaRepository<Hecho,Long> {

  /*
  void save(Hecho hecho);

  void delete(Hecho hecho);

  Long generarNuevoId();

  Hecho findById(Long id);

  List<Hecho> findAll();

   */
}
