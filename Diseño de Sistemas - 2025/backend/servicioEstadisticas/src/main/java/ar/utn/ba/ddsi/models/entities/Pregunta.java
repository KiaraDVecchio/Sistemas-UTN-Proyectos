package ar.utn.ba.ddsi.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pregunta")
@NoArgsConstructor
@Getter
@Setter
public class Pregunta {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="pregunta", nullable=false)
    private String pregunta;

    @Column(name="descripcion")
    private String descripcion;

    public Pregunta(String pregunta, String descripcion) {
      this.pregunta = pregunta;
      this.descripcion = descripcion;
    }
}
