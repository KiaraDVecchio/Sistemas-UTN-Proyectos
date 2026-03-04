package ar.utn.ba.ddsi.services;

import ar.utn.ba.ddsi.models.dtos.input.HechoInputDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;

public interface IApiCatedraService {
  public Mono<List<HechoInputDTO>> obtenerHechos(int page, int size);
  Mono<HechoInputDTO> obtenerHechoPorId(long id);
}
