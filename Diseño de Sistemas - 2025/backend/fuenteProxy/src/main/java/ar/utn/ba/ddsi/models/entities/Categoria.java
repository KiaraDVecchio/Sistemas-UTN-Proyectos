package ar.utn.ba.ddsi.models.entities;

import lombok.Getter;

public class Categoria {
  @Getter private String nombre;

  public Categoria(String nombre) {
    this.nombre = nombre;
  }
}
