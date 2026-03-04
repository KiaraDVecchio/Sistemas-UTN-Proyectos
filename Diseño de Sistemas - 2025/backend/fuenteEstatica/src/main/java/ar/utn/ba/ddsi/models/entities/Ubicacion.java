package ar.utn.ba.ddsi.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Embeddable
public class Ubicacion {
  @Column(name = "latitud",  nullable = false)
  private Double latitud;

  @Column(name = "longitud", nullable = false)
  private Double longitud;

  @Column(name = "provincia")
  private String provincia;

  public Ubicacion(Double latitud, Double longitud) {
    this.latitud = latitud;
    this.longitud = longitud;
  }
}
