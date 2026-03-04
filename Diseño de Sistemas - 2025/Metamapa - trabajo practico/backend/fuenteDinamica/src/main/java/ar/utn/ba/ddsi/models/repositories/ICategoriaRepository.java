package ar.utn.ba.ddsi.models.repositories;

import ar.utn.ba.ddsi.models.entities.Categoria;
import ar.utn.ba.ddsi.models.entities.Hecho;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ICategoriaRepository extends JpaRepository<Categoria,Long> {
  Optional<Categoria> findByNombreIgnoreCase(String nombre);
  Optional<Categoria> findById(Long id);
}
