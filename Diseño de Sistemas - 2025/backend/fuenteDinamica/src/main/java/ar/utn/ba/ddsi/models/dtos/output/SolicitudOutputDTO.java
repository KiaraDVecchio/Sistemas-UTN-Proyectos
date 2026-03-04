package ar.utn.ba.ddsi.models.dtos.output;

import ar.utn.ba.ddsi.models.entities.enumerados.EstadoSolicitud;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoSolicitud;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class SolicitudOutputDTO {
  private Long id;
  private TipoSolicitud tipoSolicitud;
  private EstadoSolicitud estado;
  private LocalDate fechaSolicitud;
  private String comentario;

  private HechoOutputDTO hecho;

  private String nombreContribuyente;
  private Long idContribuyente;
}