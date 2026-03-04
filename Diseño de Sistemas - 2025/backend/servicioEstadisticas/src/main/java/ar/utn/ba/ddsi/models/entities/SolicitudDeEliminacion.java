package ar.utn.ba.ddsi.models.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor
public class SolicitudDeEliminacion {
  private Long id;
  private Boolean esSpam;
  private LocalDateTime fechaEntrada;
  private LocalDateTime fechaAtencion;
}
