package ar.utn.ba.ddsi.controllers;

import ar.utn.ba.ddsi.models.dtos.input.HechoInputDTO;
import ar.utn.ba.ddsi.models.dtos.output.HechoOutputDTO;
import ar.utn.ba.ddsi.services.IProxyService;
import ar.utn.ba.ddsi.services.impl.ApiCatedraService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hechos")
public class ApiCatedraController {

  private final ApiCatedraService apiCatedraService;
  private IProxyService proxyService;

  public ApiCatedraController(ApiCatedraService apiCatedraService, IProxyService proxyService) {
    this.apiCatedraService = apiCatedraService;
    this.proxyService = proxyService;
  }

  @GetMapping
  public List<HechoOutputDTO> obtenerHechos(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size) {

    return apiCatedraService.obtenerHechos(page, size)
        .map(list -> list.stream()
            .map(h -> proxyService.hechoOutputDTO(h, "Catedra"))
            .collect(Collectors.toList()))
        .block();
  }


  @GetMapping("/{id}")
  public Mono<HechoInputDTO> obtenerHechoPorId(@PathVariable long id) {
    return apiCatedraService.obtenerHechoPorId(id);
  }
}
