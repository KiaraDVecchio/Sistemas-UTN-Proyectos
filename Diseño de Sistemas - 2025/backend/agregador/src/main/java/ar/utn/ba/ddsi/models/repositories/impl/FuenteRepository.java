/*package ar.utn.ba.ddsi.models.repositories.impl;

import ar.utn.ba.ddsi.models.entities.Fuente;
import ar.utn.ba.ddsi.models.repositories.IFuenteRepository;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class FuenteRepository implements IFuenteRepository {
  private List<Fuente> fuentes = new ArrayList<>();
  private final AtomicLong seq = new AtomicLong(1L);

  @Override
  public Fuente save(Fuente fuente){
    if (fuente.getId() == null) {
      fuente.setId(generarNuevoId());
    }
    fuentes.add(fuente);
    return fuente;
  }

  @Override
  public List<Fuente> findAll(){
    return this.fuentes;
  }

  public Fuente findById(Long id){
    return this.fuentes.stream().filter(f->f.getId().equals(id)).findFirst().orElse(null);
  }

  public Long generarNuevoId() {
    return fuentes.stream()
        .mapToLong(Fuente::getId)
        .max()
        .orElse(0L) + 1; // si la lista está vacía (O de valor Long), empezamos desde ID 1
  }
}
}*/
