package ar.utn.ba.ddsi.services;

import ar.utn.ba.ddsi.models.dtos.output.SolicitudOutputDTO;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.enumerados.EstadoSolicitud;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoSolicitud;
import java.util.List;

public interface ISolicitudService {
  //void solicitarEdicion(Long idHecho, HechoInputDTO nuevoHecho);
  List<SolicitudOutputDTO> obtenerTodas();
  void create(Hecho hecho, TipoSolicitud tipo);
  void atencionDeSolicitud(Long idSolicitud, EstadoSolicitud estado, String comentario, Long idAdministrador);
    boolean existeSolicitudPendiente(Long idHecho, TipoSolicitud tipo);
}
