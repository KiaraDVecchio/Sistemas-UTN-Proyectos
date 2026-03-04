/*package ar.utn.ba.ddsi.models.repositories.impl;

import ar.utn.ba.ddsi.models.entities.SolicitudDeEliminacion;
import ar.utn.ba.ddsi.models.entities.enumerados.EstadoSolicitud;
import ar.utn.ba.ddsi.models.repositories.ISolicitudRepository;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SolicitudRepository implements ISolicitudRepository {
    private List<SolicitudDeEliminacion> solicitudes = new ArrayList<>();

    @Override
    public SolicitudDeEliminacion save(SolicitudDeEliminacion solicitud){
      solicitudes.add(solicitud);
      return solicitud;
    }

    @Override
    public List<SolicitudDeEliminacion> findAll(){
      return this.solicitudes;
    }

  @Override
  public List<SolicitudDeEliminacion> findByEstado(EstadoSolicitud estado){
      return solicitudes.stream().filter(s -> s.getEstado() == estado).toList();
  }

  @Override
  public SolicitudDeEliminacion findById(Long id){
    return solicitudes.stream()
        .filter(s -> s.getId().equals(id))
        .findFirst()
        .orElse(null);
  }
}
*/