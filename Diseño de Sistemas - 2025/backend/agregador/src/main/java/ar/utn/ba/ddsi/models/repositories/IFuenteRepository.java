package ar.utn.ba.ddsi.models.repositories;

import ar.utn.ba.ddsi.models.entities.Coleccion;
import ar.utn.ba.ddsi.models.entities.Fuente;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoFuente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IFuenteRepository extends JpaRepository<Fuente,Long> {
  /*public Fuente save(Fuente fuente);
  public List<Fuente> findAll();
  public Fuente findById(Long id);*/
  List<Fuente> findByTipo(TipoFuente tipo);
}
