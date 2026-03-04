package ar.utn.ba.ddsi.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "hecho_estado_previo")
@Setter
@Getter
@NoArgsConstructor
public class HechoEstadoPrevio {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "hecho_id", nullable = false)
  private Hecho hecho;

  @Column(nullable = false)
  private String titulo;

  @Column(nullable = false)
  private String descripcion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "categoria_id", nullable = false)
  private Categoria categoria;

  @Embedded
  private Ubicacion ubicacion;

  @Column(name = "fecha_acontecimiento", nullable = false)
  private LocalDateTime fechaAcontecimiento;

  @Column(name = "fecha_edicion", nullable = false)
  private LocalDateTime fechaEdicion;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "hecho_estado_previo_id")
  private List<ContenidoMultimedia> contenidosMultimedia = new ArrayList<>();


  public HechoEstadoPrevio(Hecho hecho) {
    this.hecho = hecho;
    this.titulo = hecho.getTitulo();
    this.descripcion = hecho.getDescripcion();
    this.categoria = hecho.getCategoria();
    this.ubicacion = hecho.getUbicacion();
    this.fechaAcontecimiento = hecho.getFechaAcontecimiento();
    this.fechaEdicion = LocalDateTime.now();

    if (hecho.getContenidosMultimedia() != null) {
      for (ContenidoMultimedia c : hecho.getContenidosMultimedia()) {
        ContenidoMultimedia copia = new ContenidoMultimedia();
        copia.setPath(c.getPath());
        this.contenidosMultimedia.add(copia);
      }
    }
  }
}


