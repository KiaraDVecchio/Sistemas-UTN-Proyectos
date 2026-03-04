package ar.utn.ba.ddsi.services;

import ar.utn.ba.ddsi.models.dtos.input.CategoriaInputDTO;
import ar.utn.ba.ddsi.models.entities.Categoria;

public interface ICategoriaService {
  public Categoria findCategory(CategoriaInputDTO idCategoria);
  public Categoria crear (Categoria categoria);
}
