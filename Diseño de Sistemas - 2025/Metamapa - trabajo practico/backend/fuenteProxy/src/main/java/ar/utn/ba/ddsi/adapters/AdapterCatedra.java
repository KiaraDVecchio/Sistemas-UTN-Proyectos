package ar.utn.ba.ddsi.adapters;

import ar.utn.ba.ddsi.models.dtos.input.HechoInputDTO;
import ar.utn.ba.ddsi.services.impl.ApiCatedraService;
import java.util.List;

//ADAPTADOR
public class AdapterCatedra implements IFuenteProxyAdapter {


  private ApiCatedraService apiCatedraService;


  public AdapterCatedra(ApiCatedraService apiCatedraService, IFuenteProxyAdapter adapter) {
    this.apiCatedraService = apiCatedraService;
  }

  @Override
  public List<HechoInputDTO> getHechos() {
    List<HechoInputDTO> hechosDTO = apiCatedraService.obtenerHechos(1, 10).block(); //por defecto 10 por página

    if (hechosDTO != null) {
      hechosDTO.forEach(hDTO -> hDTO.setFuenteExterna("API_CATEDRA"));
      return hechosDTO;
    }
    else {
      return List.of();
    }
  }
}
