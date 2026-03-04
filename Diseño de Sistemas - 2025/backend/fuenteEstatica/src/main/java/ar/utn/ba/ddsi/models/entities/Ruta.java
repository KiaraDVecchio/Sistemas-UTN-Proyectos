package ar.utn.ba.ddsi.models.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ruta")
@NoArgsConstructor
@Getter
@Setter
public class Ruta {

  @Column(name = "path", nullable = false)
  String path;
  
  @Column(name = "nombre", nullable = false)
  String nombre;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long idRuta;
}
