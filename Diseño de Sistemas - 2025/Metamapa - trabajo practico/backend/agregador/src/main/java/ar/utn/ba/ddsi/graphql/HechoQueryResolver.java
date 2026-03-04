package ar.utn.ba.ddsi.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import ar.utn.ba.ddsi.services.impl.AgregadorService;
import ar.utn.ba.ddsi.models.dtos.input.FiltroHechosInput;
import ar.utn.ba.ddsi.models.dtos.output.HechoOutputDTO;

import java.util.List;

@Controller
public class HechoQueryResolver {

  private final AgregadorService agregadorService;

  public HechoQueryResolver(AgregadorService agregadorService) {
    this.agregadorService = agregadorService;
  }

  @QueryMapping
  public List<HechoOutputDTO> hechos(@Argument FiltroHechosInput filtro) {
    return agregadorService.obtenerHechosConFiltro(filtro);
  }

  @QueryMapping
  public HechoOutputDTO hechoPorId(@Argument Long id) {
    return agregadorService.obtenerHechos()
        .stream()
        .filter(h -> h.getId().equals(id))
        .findFirst()
        .orElse(null);
  }

}
