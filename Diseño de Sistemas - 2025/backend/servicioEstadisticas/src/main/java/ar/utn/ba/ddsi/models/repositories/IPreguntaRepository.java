package ar.utn.ba.ddsi.models.repositories;

import ar.utn.ba.ddsi.models.entities.Pregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IPreguntaRepository extends JpaRepository<Pregunta, Long> {
  Optional<Pregunta> findByPregunta(String pregunta);
}
