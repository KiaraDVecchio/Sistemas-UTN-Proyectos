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
  @Column(name = "latitud",  nullable = true)
   private Double latitud;

  @Column(name = "longitud", nullable = true)
  private Double longitud;

  @Column(name = "provincia")
  private String provincia;

  public Ubicacion(Double latitud, Double longitud, String provincia) {
    this.latitud = latitud;
    this.longitud = longitud;
    this.provincia = provincia;
  }
}
