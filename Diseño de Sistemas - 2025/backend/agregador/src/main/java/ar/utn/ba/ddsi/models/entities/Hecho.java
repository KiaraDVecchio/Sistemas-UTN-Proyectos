package ar.utn.ba.ddsi.models.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import ar.utn.ba.ddsi.models.entities.enumerados.Origen;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoAlgoritmoDeConsenso;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "hecho")
public class Hecho {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "titulo", nullable = false)
   private String titulo;

  @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
   private String descripcion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "categoria_id", nullable = false)
   private Categoria categoria;

  @Embedded
  private Ubicacion ubicacion;

  @Column(name = "fecha_acontecimiento", nullable = false)
  private LocalDateTime fechaAcontecimiento;

  @Column(name = "fecha_carga", nullable = false)
  private java.time.LocalDate fechaCarga;

  @Enumerated(EnumType.STRING)
  @Column(name = "origen")
   private Origen origen;

  @Column(name = "fue_eliminado")
  private Boolean fueEliminado;

  @ManyToMany
  @JoinTable(name = "hecho_etiqueta",
  joinColumns = @JoinColumn(name = "hecho_id", referencedColumnName = "id"),
  inverseJoinColumns = @JoinColumn(name = "etiqueta_id",
  referencedColumnName = "id")
  )
   private Set<Etiqueta> etiquetas = new HashSet<>(); //TODO revisar si funciona bien si lo inicializo aca

   @Column(name = "fuente_externa", nullable = true)
   private String fuenteExterna;

   @ElementCollection
   @CollectionTable(name = "hecho_consenso", joinColumns = @JoinColumn(name = "hecho_id" ))
   @MapKeyEnumerated(EnumType.STRING)
   @MapKeyColumn(name = "algoritmo")
   @Column(name = "aprobado", nullable = false)
   private Map<TipoAlgoritmoDeConsenso, Boolean> consensoPorAlgoritmo = new EnumMap<>(TipoAlgoritmoDeConsenso.class); //mismo chequeo que arriba

    @Embedded
    private Contribuyente contribuyente;

  @ElementCollection
  @CollectionTable(name = "path_multimedia", joinColumns = @JoinColumn(name = "hecho_id"))
  @Column(name = "path_multimedia")
   private List<String> pathMultimedia = new ArrayList<>(); //mismo

  @Column(name = "rutaNombre", nullable = true)
     private String rutaNombre;

  @Column(name = "fechaActualizacion", nullable = true)
   private LocalDate fechaActualizacion;

  @Column(name = "cantidad_vistas")
  private Integer cantVistas;

  @Column(name = "idEnFuente")
  private Long idEnFuente;

    @Column(name = "tiene_edicion_pendiente")
    private Boolean tieneEdicionPendiente = false;

  //@Column(name = "editable" )
  //private boolean editable;

  //@Column(name = "diasRestantesEdicion")
  //private int diasRestantesEdicion;

  // private String nombreAportante;
  // private String apellidoAportante;
  // private Integer edadAportante;

  public Hecho(String titulo, String descripcion, Categoria categoria, Ubicacion ubicacion, LocalDateTime fechaAcontecimiento, LocalDate fechaCarga , Origen origen, String fuenteExterna){
    this.titulo = titulo;
    this.descripcion = descripcion;
    this.categoria = categoria;
    this.ubicacion = ubicacion;
    this.fechaAcontecimiento = fechaAcontecimiento;
    this.fechaCarga = fechaCarga;
    this.origen = origen;
    this.fueEliminado = false;
    this.etiquetas = new HashSet<>();
    this.fuenteExterna = fuenteExterna;
    this.consensoPorAlgoritmo = new HashMap<>();
    this.idEnFuente = null;
  }

  public void agregarEtiqueta(Etiqueta etiqueta) {
    this.etiquetas.add(etiqueta);
  }

  public boolean esIgualContenido(Hecho otroHecho) {
    if (otroHecho == null){
      return false;
    }

    return Objects.equals(this.titulo.trim(), otroHecho.titulo.trim()) &&
        Objects.equals(this.descripcion.trim(), otroHecho.descripcion.trim()) &&
        Objects.equals(this.categoria.getNombre().trim().toUpperCase(), otroHecho.categoria.getNombre().trim().toUpperCase()) &&
        Objects.equals(this.ubicacion.getLatitud(), otroHecho.ubicacion.getLatitud()) &&
        Objects.equals(this.ubicacion.getLongitud(), otroHecho.ubicacion.getLongitud()) &&
        Objects.equals(this.fechaAcontecimiento.toLocalDate(), otroHecho.fechaAcontecimiento.toLocalDate());
  }

  public boolean esConsensuado(TipoAlgoritmoDeConsenso algoritmo) { //esta basico dsp hay que agregar q si no hay algoritmo sea true o algo asi
    return this.consensoPorAlgoritmo.getOrDefault(algoritmo, true);
  }

  public void setConsensoParaAlgoritmo(TipoAlgoritmoDeConsenso algoritmo, boolean consensuado){
    this.consensoPorAlgoritmo.put(algoritmo, consensuado);
  }

  public void limpiarConsensos() {
    this.consensoPorAlgoritmo.clear();
  }

  public boolean huboEdicion(Hecho otro) {
    if (otro == null) return false;

    boolean tituloCambio = !Objects.equals(this.titulo, otro.titulo);
    boolean descripcionCambio = !Objects.equals(this.descripcion, otro.descripcion);

    boolean categoriaCambio = !Objects.equals(
        this.categoria != null ? this.categoria.getNombre() : null,
        otro.categoria != null ? otro.categoria.getNombre() : null
    );

    boolean fechaAcontecimientoCambio = !Objects.equals(this.fechaAcontecimiento, otro.fechaAcontecimiento);

    boolean ubicacionCambio = false;
    if (this.ubicacion != null && otro.ubicacion != null) {
      ubicacionCambio =
          !Objects.equals(this.ubicacion.getLatitud(), otro.ubicacion.getLatitud()) ||
              !Objects.equals(this.ubicacion.getLongitud(), otro.ubicacion.getLongitud()) ||
              !Objects.equals(this.ubicacion.getProvincia(), otro.ubicacion.getProvincia());
    } else if (this.ubicacion != otro.ubicacion) {
      ubicacionCambio = true;
    }

    boolean multimediaCambio = !Objects.equals(this.pathMultimedia, otro.pathMultimedia);

    boolean eliminadoCambio = !Objects.equals(this.fueEliminado, otro.fueEliminado);

    return tituloCambio ||
        descripcionCambio ||
        categoriaCambio ||
        fechaAcontecimientoCambio ||
        ubicacionCambio ||
        multimediaCambio ||
        eliminadoCambio;
  }

  public void actualizarDesde(Hecho otro) {
    if (otro == null) return;

    this.titulo = otro.titulo;
    this.descripcion = otro.descripcion;
    this.categoria = otro.categoria;

    this.fechaAcontecimiento = otro.fechaAcontecimiento;

    // Actualizar ubicación campo por campo
    if (this.ubicacion != null && otro.ubicacion != null) {
      this.ubicacion.setLatitud(otro.ubicacion.getLatitud());
      this.ubicacion.setLongitud(otro.ubicacion.getLongitud());
      this.ubicacion.setProvincia(otro.ubicacion.getProvincia());
    } else {
      this.ubicacion = otro.ubicacion;
    }

      if (otro.getTieneEdicionPendiente() != null) {
          this.tieneEdicionPendiente = otro.getTieneEdicionPendiente();
      }

    this.pathMultimedia = otro.pathMultimedia != null ?
        new ArrayList<>(otro.pathMultimedia) :
        new ArrayList<>();


    this.fechaActualizacion = LocalDate.now();
  }


}