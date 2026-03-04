package ar.utn.ba.ddsi.controllers;

import ar.utn.ba.ddsi.models.dtos.input.ColeccionInputDTO;
import ar.utn.ba.ddsi.models.dtos.input.HechoInputDTO;
import ar.utn.ba.ddsi.models.dtos.input.SolicitudInputDTO;
import ar.utn.ba.ddsi.models.dtos.output.HechoOutputDTO;
import ar.utn.ba.ddsi.services.IProxyService;
import ar.utn.ba.ddsi.services.impl.ApiMetaMapaService;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping
public class ApiMetaMapaController {
  private final PropertyResourceConfigurer propertyResourceConfigurer;
  private ApiMetaMapaService apiMetaMapaService;
  private IProxyService proxyService;

  public ApiMetaMapaController(ApiMetaMapaService apiMetaMapaService, IProxyService proxyService, PropertyResourceConfigurer propertyResourceConfigurer) {
    this.apiMetaMapaService = apiMetaMapaService;
    this.proxyService = proxyService;
    this.propertyResourceConfigurer = propertyResourceConfigurer;
  }

  @GetMapping("/hechos")
  public List<HechoOutputDTO> obtenerHechos() {
    return apiMetaMapaService.obtenerHechos()
        .map(list -> list.stream()
            .map(h -> proxyService.hechoOutputDTO(h , "MetaMapa"))
            .collect(Collectors.toList()))
        .block();
  }

  @GetMapping("/colecciones")
  public Mono<List<ColeccionInputDTO>> obtenerColecciones(){
    return apiMetaMapaService.obtenerColecciones();
  }

  @GetMapping("colecciones/{id}/hechos")
  public Mono<List<HechoInputDTO>> obtenerHechosDeColeccion(@PathVariable long id){
    return apiMetaMapaService.obtenerColeccionPorId(id)
        .map(ColeccionInputDTO::getHechosDeLaColeccion);
  }

  @PostMapping("/solicitudes")
  public Mono<List<SolicitudInputDTO>> crearSolicitud(@RequestBody List<SolicitudInputDTO> solicitudes) {
    return apiMetaMapaService.crearSolicitud(solicitudes);
  }


}


