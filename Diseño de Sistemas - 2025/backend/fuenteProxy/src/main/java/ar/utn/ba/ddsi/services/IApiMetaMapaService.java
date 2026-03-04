package ar.utn.ba.ddsi.services;

import ar.utn.ba.ddsi.models.dtos.input.ColeccionInputDTO;
import ar.utn.ba.ddsi.models.dtos.input.HechoInputDTO;
import ar.utn.ba.ddsi.models.dtos.input.SolicitudInputDTO;
import reactor.core.publisher.Mono;
import java.util.List;

public interface IApiMetaMapaService {
  Mono<List<HechoInputDTO>> obtenerHechos();
  Mono<ColeccionInputDTO> obtenerColeccionPorId(long id);
  Mono<List<ColeccionInputDTO>> obtenerColecciones();
  Mono<List<SolicitudInputDTO>> crearSolicitud(List<SolicitudInputDTO>  solicitudes);
}
