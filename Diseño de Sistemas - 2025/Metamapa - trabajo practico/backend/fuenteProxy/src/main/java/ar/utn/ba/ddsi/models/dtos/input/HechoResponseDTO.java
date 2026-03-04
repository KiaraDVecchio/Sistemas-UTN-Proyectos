package ar.utn.ba.ddsi.models.dtos.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class HechoResponseDTO {
  // Anota “data” porque ese es el nombre real del campo en el JSON
  @JsonProperty("data")
  private List<HechoInputDTO> data;
}