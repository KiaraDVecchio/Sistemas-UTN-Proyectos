package ar.utn.ba.ddsi.models.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import ar.utn.ba.ddsi.models.entities.enumerados.Origen;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
  @JoinColumn(name = "categoria_id", nullable = false)
  private Categoria categoria;

  @Embedded
  private Ubicacion ubicacion;

  @Column(name = "fecha_acontecimiento", nullable = false)
  private LocalDateTime fechaAcontecimiento;

  @Column(name = "fecha_carga", nullable = false)
  private LocalDate fechaCarga;

  @Enumerated(EnumType.STRING)
  @Column(name = "origen")
  private Origen origen;

  @Column(name = "fue_eliminado")
  private Boolean fueEliminado;

    @Column(name = "tiene_edicion_pendiente")
    private Boolean tieneEdicionPendiente = false;

  @ManyToMany
  @JoinTable(name = "hecho_etiqueta",
      joinColumns = @JoinColumn(name = "hecho_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "etiqueta_id", referencedColumnName = "id")
  )
  private Set<Etiqueta> etiquetas = new HashSet<>();;

  @Embedded
  private Contribuyente contribuyente;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "contenido_multimedia_id")
  private List<ContenidoMultimedia> contenidosMultimedia;

  @OneToOne(mappedBy = "hecho", cascade = CascadeType.ALL, orphanRemoval = true)
  private HechoEstadoPrevio estadoPrevio;

  @Column(name = "fecha_actualizacion")
  private LocalDate fechaActualizacion;

  public Hecho(String titulo, String descripcion, Categoria categoria, Ubicacion ubicacion, LocalDateTime fechaAcontecimiento, Origen origen){
    this.titulo = titulo;
    this.descripcion = descripcion;
    this.categoria = categoria;
    this.ubicacion = ubicacion;
    this.fechaAcontecimiento = fechaAcontecimiento;
    this.fechaCarga = LocalDate.now();
    this.origen = origen;
    this.fueEliminado = false;
    this.etiquetas = new HashSet<>();
    this.contenidosMultimedia = new ArrayList<>();
    this.fechaActualizacion = null;
  }

  public void agregarEtiqueta(Etiqueta etiqueta) {
    this.etiquetas.add(etiqueta);
  }

  public void actualizarContenidosMultimedia(List<ContenidoMultimedia> nuevos) {
    // 1. Limpiamos la colección existente (Hibernate registra esto para borrar huérfanos)
    this.contenidosMultimedia.clear();

    // 2. Agregamos los nuevos (Hibernate los marcará para insertar)
    if (nuevos != null) {
      this.contenidosMultimedia.addAll(nuevos);
    }
  }

  public List<String> getPathsMultimedia(List<ContenidoMultimedia> contenidos){
    if (contenidos == null) return new ArrayList<>();
    return contenidos.stream().map(ContenidoMultimedia::getPath).collect(Collectors.toList());
  }
}
