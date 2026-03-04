package ar.utn.ba.ddsi.models.entities;

import lombok.Getter;

public class Etiqueta {
  @Getter private String nombre;

  public Etiqueta(String nombre) {
    this.nombre = nombre;
  }
}
