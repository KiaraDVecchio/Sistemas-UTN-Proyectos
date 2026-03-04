package ar.utn.ba.ddsi.models.repositories;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ar.utn.ba.ddsi.models.entities.Coleccion;
import ar.utn.ba.ddsi.models.entities.Hecho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IColeccionRepository extends JpaRepository<Coleccion,String> {
    Optional<Coleccion> findByHandle(String handle);
    @Query("SELECT c FROM Coleccion c LEFT JOIN FETCH c.hechos")
    List<Coleccion> findAllWithHechos();
    void deleteByHandle(String handle);
    List<Coleccion> findTop4ByOrderByCantVistasDesc();
    Page<Coleccion> findByTituloContainingIgnoreCaseOrDescripcionContainingIgnoreCase(String titulo, String descripcion, Pageable pageable);
}