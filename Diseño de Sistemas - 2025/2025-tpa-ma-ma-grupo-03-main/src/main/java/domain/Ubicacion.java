package domain;

import lombok.Getter;

public class Ubicacion {
  @Getter private Double latitud;
  @Getter private Double longitud;

  public Ubicacion(Double latitud, Double longitud) {
    this.latitud = latitud;
    this.longitud = longitud;
  }
}
