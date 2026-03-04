package ar.utn.ba.ddsi.models.repositories;

import ar.utn.ba.ddsi.models.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IUsuariosRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findBymail(String username);
}