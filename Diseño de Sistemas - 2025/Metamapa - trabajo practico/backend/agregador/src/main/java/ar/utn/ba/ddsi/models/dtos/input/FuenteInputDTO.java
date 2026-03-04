package ar.utn.ba.ddsi.models.dtos.input;

import ar.utn.ba.ddsi.models.entities.Fuente;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoFuente;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FuenteInputDTO {
  private String url;
  private TipoFuente tipo;

  public static FuenteInputDTO fromEntity(Fuente f) {
    FuenteInputDTO dto = new FuenteInputDTO();
    dto.setUrl(f.getUrl());
    dto.setTipo(f.getTipo());
    return dto;
  }
}