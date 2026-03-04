package ar.utn.ba.ddsi.models.entities;

import ar.utn.ba.ddsi.models.entities.enumerados.EstadoSolicitud;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoSolicitud;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitud")
@NoArgsConstructor
@Getter
@Setter
public class Solicitud {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_solicitud")
  private TipoSolicitud tipoSolicitud;

  @ManyToOne
  @JoinColumn(name = "hecho_id", nullable = false)
  private Hecho hecho;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado")
  private EstadoSolicitud estado;

  @Column(name = "fecha_solicitud")
  private LocalDate fechaSolicitud;

  @Column(name = "fecha_atencion")
  private LocalDate fechaAtencion;

  @Column(name = "comentario")
  private String comentario;

  @Column(name = "id_administrador")
  private Long idAdministradorQueAtendio;

  public Solicitud(Hecho hecho, TipoSolicitud tipo) {
    this.hecho = hecho;
    this.comentario = null;
    this.estado = EstadoSolicitud.PENDIENTE;
    this.idAdministradorQueAtendio = null;
    this.fechaSolicitud = LocalDate.now();
    this.fechaAtencion = null;
    this.tipoSolicitud = tipo;
  }

  public void cambiarEstado(EstadoSolicitud nuevoEstado){
    this.estado = nuevoEstado;
  }
}