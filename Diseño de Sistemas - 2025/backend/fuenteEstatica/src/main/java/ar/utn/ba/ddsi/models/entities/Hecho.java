package ar.utn.ba.ddsi.models.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import ar.utn.ba.ddsi.models.entities.enumerados.Origen;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "hecho")
@NoArgsConstructor
@Getter
@Setter
public class Hecho {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "titulo", nullable = false)
  private String titulo;

  @Column(name = "descripcion", nullable = false)
  private String descripcion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "categoria_id")
  private Categoria categoria;

  @Embedded
  private Ubicacion ubicacion;

  @Column(name = "fechaAcontecimiento", nullable = false)
  private LocalDate fechaAcontecimiento;

  @Column(name = "fechaCarga", nullable = false)
  private LocalDate fechaCarga;

  @Enumerated(EnumType.STRING)
  @Column(name = "origen")
  private Origen origen;

  @Column(name = "fue_eliminado")
  private boolean fueEliminado;

  @ManyToMany
  @JoinTable(name = "hecho_etiqueta",
      joinColumns = @JoinColumn(name = "hecho_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "etiqueta_id", referencedColumnName = "id"))
  private Set<Etiqueta> etiquetas;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  private Ruta ruta;

  public Hecho(String titulo, String descripcion, Categoria categoria, Ubicacion ubicacion, LocalDate fechaAcontecimiento, Origen origen){
    this.titulo = titulo;
    this.descripcion = descripcion;
    this.categoria = categoria;
    this.ubicacion = ubicacion;
    this.fechaAcontecimiento = fechaAcontecimiento;
    this.fechaCarga = LocalDate.now();
    this.origen = origen;
    this.fueEliminado = false;
    this.etiquetas = new HashSet<>();
  }

  public void agregarEtiqueta(Etiqueta etiqueta) {
    this.etiquetas.add(etiqueta);
  }

}
