package ar.utn.ba.ddsi.models.repositories.impl;

import ar.utn.ba.ddsi.models.dtos.output.HechoOutputDTO;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.repositories.IHechoRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
/*
@Repository
public class HechoRepository implements IHechoRepository {
  private List<Hecho> hechos;

  public HechoRepository() {
    this.hechos = new ArrayList<Hecho>();
  }

  @Override
  public void save(Hecho hecho) {

    // TODO es responsabilidad de esta capa hacer la verifiacion de si el hecho tiene id?
    // TODO preguntar si se puede subir un hecho que ya existe (Notion)

    if (hecho.getId() == null) {
      hecho.setId(generarNuevoId());
    } else {
        hechos.removeIf(h -> h.getId().equals(hecho.getId()));
    }
    this.hechos.add(hecho);
  }


  @Override
  public Long generarNuevoId() {
    return hechos.stream()
        .mapToLong(Hecho::getId)
        .max()
        .orElse(0L) + 1; // si la lista está vacía (O de valor Long), empezamos desde ID 1
  }
  @Override
  public void delete(Hecho hecho) {
    this.hechos.remove(hecho);
  }

  @Override
  public Hecho findById(Long id) {
    return this.hechos.stream().filter(h -> h.getId().equals(id)).findFirst().orElse(null);
  }

  @Override
  public List<Hecho> findAll(){
    return this.hechos;
  }
}
*/