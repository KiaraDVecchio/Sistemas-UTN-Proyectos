/*package ar.utn.ba.ddsi.models.repositories.impl;

import ar.utn.ba.ddsi.models.entities.Coleccion;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.repositories.IColeccionRepository;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ColeccionRepository implements IColeccionRepository {
  private List<Coleccion> colecciones = new ArrayList<>();

  @Override
  public Coleccion save(Coleccion coleccion) {
    if (coleccion.getHandle() == null) {
      // generamos un UUID aleatorio y lo usamos directamente como handle
      String uuid = UUID.randomUUID().toString();
      coleccion.setHandle(uuid);
      colecciones.add(coleccion);
      return coleccion;
    }
    for (int i = 0; i < colecciones.size(); i++) {
      if (colecciones.get(i).getHandle().equals(coleccion.getHandle())) {
        colecciones.set(i, coleccion); //la reemplazo
        return coleccion;
      }
    }
    //caso por si el handle se habia puesto manual u otras posibilidades
    colecciones.add(coleccion);
    return coleccion;
  }
  private boolean existsHandle(String h) {
    return colecciones.stream().anyMatch(c -> h.equals(c.getHandle()));
  }

  @Override
  public List<Coleccion> findAll(){
    return new ArrayList<>(colecciones);
  }

  public void eliminarHecho(Hecho hechoEliminado){
    for(Coleccion coleccion : colecciones){
      coleccion.getHechos().remove(hechoEliminado);
    }
  }

  @Override
  public Coleccion findById(String coleccionId) {
    return colecciones.stream()
        .filter(c -> c.getHandle().equals(coleccionId))
        .findFirst()
        .orElse(null);
}

@Override
  public void deleteById(String id){
    Coleccion coleccionAEliminar = this.findById(id);
    colecciones.remove(coleccionAEliminar);
}

}*/
