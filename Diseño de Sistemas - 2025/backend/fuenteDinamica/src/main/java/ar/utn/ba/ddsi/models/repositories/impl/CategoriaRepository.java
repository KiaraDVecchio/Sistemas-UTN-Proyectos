/* package ar.utn.ba.ddsi.models.repositories.impl;

import ar.utn.ba.ddsi.models.entities.Categoria;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.repositories.ICategoriaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CategoriaRepository implements ICategoriaRepository {

  private List<Categoria> categorias = new ArrayList<>();

  @Override
  public Categoria findById(Long id) {
    return categorias
        .stream()
        .filter(c -> c.getId().equals(id))
        .findFirst()
        .orElse(null);
  }

  @Override
  public Long save(Categoria categoria) {
    Categoria yaExistente = categorias.stream().filter(c -> categoria.getNombre().equals(c.getNombre())).findFirst().orElse(null);
    if (yaExistente != null){
      return yaExistente.getId();
    }
    categoria.setId(generarNuevoId());
    this.categorias.add(categoria);
    return categoria.getId();
  }

  @Override
  public Long generarNuevoId() {
    return categorias.stream()
        .mapToLong(Categoria::getId)
        .max()
        .orElse(0L) + 1; // si la lista está vacía (O de valor Long), empezamos desde ID 1
  }
}
*/