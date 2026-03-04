package ar.utn.ba.ddsi.models.dtos.input;

import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.SolicitudDeEliminacion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SolicitudInputDTO {
  private Long hechoId;
  private String justificacion;
    private boolean esSpam;
}