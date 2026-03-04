package ar.utn.ba.ddsi.models.repositories;

import ar.utn.ba.ddsi.models.entities.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IEtiquetaRepository extends JpaRepository<Etiqueta, Long> {
}
