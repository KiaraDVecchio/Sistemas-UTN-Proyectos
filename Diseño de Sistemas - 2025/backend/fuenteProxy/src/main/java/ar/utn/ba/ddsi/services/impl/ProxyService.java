package ar.utn.ba.ddsi.services.impl;

import ar.utn.ba.ddsi.models.dtos.input.HechoInputDTO;
import ar.utn.ba.ddsi.models.dtos.output.HechoOutputDTO;
import ar.utn.ba.ddsi.models.dtos.output.UbicacionOutputDTO;
import ar.utn.ba.ddsi.models.entities.Ubicacion;
import ar.utn.ba.ddsi.services.IProxyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

@Service
public class ProxyService implements IProxyService {

  @Override
  public HechoOutputDTO hechoOutputDTO(HechoInputDTO hechoInputDTO, String fuenteExterna) {
    if (hechoInputDTO == null) {
      return null;
    }

    HechoOutputDTO dto = new HechoOutputDTO();

    dto.setTitulo(hechoInputDTO.getTitulo());
    dto.setDescripcion(hechoInputDTO.getDescripcion());
    dto.setCategoria(hechoInputDTO.getCategoria());
    dto.setLatitud(hechoInputDTO.getLatitud());
    dto.setLongitud(hechoInputDTO.getLongitud());
    dto.setFechaAcontecimiento(hechoInputDTO.getFechaAcontecimiento().toLocalDate());
    dto.setFechaCarga(hechoInputDTO.getFechaCarga().toLocalDate());

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode extras = mapper.createObjectNode();

    // Guardamos la fuente (dato extra de proxy)
    if (hechoInputDTO.getFuenteExterna() != null) {
      extras.put("fuente", fuenteExterna);
    }

    return dto;
  }
}
