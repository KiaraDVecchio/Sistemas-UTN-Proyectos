package ar.utn.ba.ddsi.services.impl;

import ar.utn.ba.ddsi.models.dtos.input.CategoriaInputDTO;
import ar.utn.ba.ddsi.models.dtos.input.HechoInputDTO;
import ar.utn.ba.ddsi.models.dtos.output.HechoOutputDTO;
import ar.utn.ba.ddsi.models.entities.Categoria;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.Ubicacion;
import ar.utn.ba.ddsi.models.entities.enumerados.Origen;
import ar.utn.ba.ddsi.models.repositories.ICategoriaRepository;
import ar.utn.ba.ddsi.services.ICategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class CategoriaService implements ICategoriaService {

  @Autowired
  private ICategoriaRepository categoriaRepository;

  /*@Override
  public Categoria findCategory(CategoriaInputDTO categoriaInputDTO){
    if (categoriaInputDTO.getId() == null){
      Categoria categoria = new Categoria(categoriaInputDTO.getNombre().toUpperCase());
      return this.crear(categoria);
    }
    else{
      if (categoriaInputDTO.getNombre() == null) {
        throw new NoSuchElementException("Categoria no encontrada");
      }
    return categoriaRepository.findById(categoriaInputDTO.getId()).orElse(null);
    }
  }*/

  @Override
  public Categoria findCategory(CategoriaInputDTO categoriaInputDTO){
    Categoria categoria = categoriaRepository.findByNombreIgnoreCase(categoriaInputDTO.getNombre())
        .orElseGet(() -> {
          Categoria c = new Categoria();
          c.setNombre(categoriaInputDTO.getNombre());
          return categoriaRepository.save(c);
        });
    return categoria;
  }

  public Categoria crear (Categoria categoria){
     categoria.setId(this.categoriaRepository.save(categoria).getId());
     return categoria;
  }
}