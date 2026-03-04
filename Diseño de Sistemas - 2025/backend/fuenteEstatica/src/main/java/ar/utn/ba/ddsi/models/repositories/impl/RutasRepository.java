/*package ar.utn.ba.ddsi.models.repositories.impl;

import ar.utn.ba.ddsi.models.entities.Ruta;
import ar.utn.ba.ddsi.models.repositories.IRutasRepository;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RutasRepository implements IRutasRepository {
  private List<Ruta> rutas;

  public RutasRepository() {
    this.rutas = new ArrayList<Ruta>();
  }

  @Override
  public void save(Ruta ruta) {
    if (ruta.getIdRuta() == null) {
      ruta.setIdRuta(generarNuevoId());
    } else{
      rutas.removeIf(h -> h.getIdRuta().equals(ruta.getIdRuta()));
    }
    this.rutas.add(ruta);
  }

  @Override
  public Ruta findById(Long id) {
    return this.rutas.stream().filter(h -> h.getIdRuta().equals(id)).findFirst().orElse(null);
  }

  @Override
  public Long generarNuevoId() {
    return rutas.stream()
        .mapToLong(Ruta::getIdRuta)
        .max()
        .orElse(0L) + 1; // si la lista está vacía (O de valor Long), empezamos desde ID 1
  }

  @Override
  public List<Ruta> findAll(){
    return this.rutas;
  }
}*/
