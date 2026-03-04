package ar.utn.ba.ddsi.services;

import ar.utn.ba.ddsi.models.dtos.input.SolicitudInputDTO;
import ar.utn.ba.ddsi.models.entities.SolicitudDeEliminacion;
import ar.utn.ba.ddsi.models.entities.enumerados.EstadoSolicitud;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ISolicitudService {
   SolicitudDeEliminacion RechazadoPorSpam(SolicitudDeEliminacion solicitud);
   void aceptarSolicitud(SolicitudDeEliminacion solicitud);
   void rechazarSolicitud(SolicitudDeEliminacion solicitud);
    SolicitudDeEliminacion crearSolicitud(SolicitudInputDTO solicitud);

  Long contarPorEstado(EstadoSolicitud estadoSolicitud);


}
