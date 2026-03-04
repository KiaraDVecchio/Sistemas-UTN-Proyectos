package ar.utn.ba.ddsi.models.dtos.output;

import ar.utn.ba.ddsi.models.entities.Fuente;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FuenteOutputDTO {
  private Long id;
  private String tipo;
  private String url;


  public static FuenteOutputDTO fromEntity(Fuente f) {
    FuenteOutputDTO dto = new FuenteOutputDTO();
    dto.setId(f.getId());
    dto.setTipo(f.getTipo().name());
    dto.setUrl(f.getUrl());
    return dto;
  }
}