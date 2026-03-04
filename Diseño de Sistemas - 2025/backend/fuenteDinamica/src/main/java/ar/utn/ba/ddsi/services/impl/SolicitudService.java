package ar.utn.ba.ddsi.services.impl;

import ar.utn.ba.ddsi.models.dtos.output.HechoOutputDTO;
import ar.utn.ba.ddsi.models.dtos.output.SolicitudOutputDTO;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.Solicitud;
import ar.utn.ba.ddsi.models.entities.enumerados.EstadoSolicitud;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoSolicitud;
import ar.utn.ba.ddsi.models.repositories.ISolicitudRepository;
import ar.utn.ba.ddsi.services.IHechoService;
import ar.utn.ba.ddsi.services.ISolicitudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SolicitudService implements ISolicitudService {

  @Autowired
  private ISolicitudRepository solicitudRepository;
  @Autowired
  private IHechoService hechoService;

    @Override
    public void create(Hecho hecho, TipoSolicitud tipo){
      Solicitud solicitud = new Solicitud(hecho, tipo);
      this.solicitudRepository.save(solicitud);
    }

    @Override
    public void atencionDeSolicitud(Long idSolicitud, EstadoSolicitud estado, String comentario, Long idAdministrador){
      Solicitud solicitud = solicitudRepository.findById(idSolicitud).orElse(null);
      solicitud.cambiarEstado(estado);
      solicitud.setComentario(comentario);

      switch (estado) {
        case ACEPTADA:
          solicitud.setEstado(EstadoSolicitud.ACEPTADA);
          solicitud.getHecho().setTieneEdicionPendiente(false);
          guardadoDeCredencial(solicitud, comentario, idAdministrador);
          break;
        case RECHAZADA:
            solicitud.setEstado(EstadoSolicitud.RECHAZADA);
            solicitud.getHecho().setTieneEdicionPendiente(false);
            guardadoDeCredencial(solicitud, comentario, idAdministrador);
          if (solicitud.getTipoSolicitud() == TipoSolicitud.CREACION){
            this.hechoService.creacionRechazada(solicitud.getHecho());
          }
          else{
            this.hechoService.edicionRechazada(solicitud.getHecho());
          }
          break;
      }
      solicitudRepository.save(solicitud);

    }

    public void guardadoDeCredencial(Solicitud solicitud ,String comentario, Long idAdministrador){
      solicitud.setComentario(comentario);
      solicitud.setIdAdministradorQueAtendio(idAdministrador);
      solicitud.setFechaAtencion(LocalDate.now());
    }

  @Override
  public List<SolicitudOutputDTO> obtenerTodas() {
    List<Solicitud> solicitudes = solicitudRepository.findAll();

    return solicitudes.stream()
        .map(this::mapearASolicitudOutput)
        .collect(Collectors.toList());
  }

  private SolicitudOutputDTO mapearASolicitudOutput(Solicitud s) {
    SolicitudOutputDTO dto = new SolicitudOutputDTO();
    dto.setId(s.getId());
    dto.setTipoSolicitud(s.getTipoSolicitud());
    dto.setEstado(s.getEstado());
    dto.setFechaSolicitud(s.getFechaSolicitud());
    dto.setComentario(s.getComentario());

    if (s.getHecho() != null) {
      HechoOutputDTO hDto = hechoService.hechoOutputDTO(s.getHecho());
      dto.setHecho(hDto);

      if (s.getHecho().getContribuyente() != null) {
        dto.setNombreContribuyente(s.getHecho().getContribuyente().getNombre() + " " + s.getHecho().getContribuyente().getApellido());
        dto.setIdContribuyente(s.getHecho().getContribuyente().getIdContribuyente());
      }
    }
    return dto;
  }
    @Override
    public boolean existeSolicitudPendiente(Long idHecho, TipoSolicitud tipo) {
        return solicitudRepository.existsByHecho_IdAndTipoSolicitudAndEstado(
                idHecho,
                tipo,
                EstadoSolicitud.PENDIENTE
        );
    }

}