package ar.utn.ba.ddsi.models.dtos.output;

import ar.utn.ba.ddsi.models.entities.SolicitudDeEliminacion;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class SolicitudOutputDTO {
  private Long id;
  private String estado;
  private String justificacion;
  private LocalDateTime fechaEntrada;
  private LocalDateTime fechaAtencion;
  private Long hechoId;
    private boolean esSpam;

  public static SolicitudOutputDTO fromEntity(SolicitudDeEliminacion s) {
    SolicitudOutputDTO dto = new SolicitudOutputDTO();
    dto.setId(s.getId());
    dto.setEstado(String.valueOf(s.getEstado()));
    dto.setJustificacion(s.getJustificacion());
    dto.setFechaEntrada(s.getFechaEntrada());
    dto.setFechaAtencion(s.getFechaAtencion());
    dto.setHechoId(s.getHecho().getId());
    dto.esSpam = s.isEsSpam();
    return dto;
  }
}