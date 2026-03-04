package ar.utn.ba.ddsi.services;

import ar.utn.ba.ddsi.models.entities.SolicitudDeEliminacion;
import org.springframework.stereotype.Component;

@Component
public interface IDetectorDeSpam {
  boolean esSpam(String texto);
}
