package ar.utn.ba.ddsi.services.impl;

import ar.utn.ba.ddsi.models.dtos.input.HechoInputDTO;
import ar.utn.ba.ddsi.models.dtos.input.HechoResponseDTO;
import ar.utn.ba.ddsi.services.IApiCatedraService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ApiCatedraService implements IApiCatedraService {

  private final WebClient webClient;

  public ApiCatedraService(@Qualifier("apiClient") WebClient webClient) {
    this.webClient = webClient;
  }

  // Clase auxiliar para llevar el número de página junto con los datos
  record PaginaConDatos(int numeroPagina, HechoResponseDTO datos) {}

  @Override
  public Mono<List<HechoInputDTO>> obtenerHechos(int page, int size) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/desastres")
            .queryParam("page", page)
            .queryParam("size", size)
            .build())
        .retrieve()
        .bodyToMono(HechoResponseDTO.class)
        .map(HechoResponseDTO::getData);
  }

  private Mono<PaginaConDatos> obtenerPagina(int numeroPagina) {
    return webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/desastres")
            .queryParam("page", numeroPagina)
            .build())
        .retrieve()
        .bodyToMono(HechoResponseDTO.class)
        .map(response -> new PaginaConDatos(numeroPagina, response));
  }


  @Override
  public Mono<HechoInputDTO> obtenerHechoPorId(long id) {
    return webClient.get()
        .uri("/desastres/{id}", id)
        .retrieve()
        .bodyToMono(HechoInputDTO.class);
  }
}