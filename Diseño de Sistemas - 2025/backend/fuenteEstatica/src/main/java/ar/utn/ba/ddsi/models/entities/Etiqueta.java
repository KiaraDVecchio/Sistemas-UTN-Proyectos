package ar.utn.ba.ddsi.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Table(name = "etiqueta")
@Getter
@Setter
public class Etiqueta {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "nombre" , nullable = false)
  private String nombre;

  public Etiqueta(String nombre) {
    this.nombre = nombre;
  }
}
