package ar.utn.ba.ddsi.models.dtos.input;

import lombok.Data;
import java.util.List;

@Data
public class SolicitudResponseDTO {
  private List<SolicitudInputDTO> data;
}
