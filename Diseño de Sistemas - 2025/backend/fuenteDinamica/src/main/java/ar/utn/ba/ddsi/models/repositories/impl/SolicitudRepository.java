package ar.utn.ba.ddsi.models.repositories.impl;

import ar.utn.ba.ddsi.models.dtos.input.HechoInputDTO;
import ar.utn.ba.ddsi.models.dtos.output.HechoOutputDTO;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.Solicitud;
import ar.utn.ba.ddsi.models.repositories.ISolicitudRepository;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
/*
@Repository
public class SolicitudRepository implements ISolicitudRepository {

  private Map<Long, Solicitud> solicitudes = new HashMap<>();
  private Long siguienteId = 1L;

  @Override
  public void save(Solicitud solicitud) {
    if (solicitud.getId() == null) {
      solicitud.setId(siguienteId++);
    }
    solicitudes.put(solicitud.getId(), solicitud);
  }

  @Override
  public Solicitud findById(Long id) {
    return solicitudes.get(id);
  }

}
*/