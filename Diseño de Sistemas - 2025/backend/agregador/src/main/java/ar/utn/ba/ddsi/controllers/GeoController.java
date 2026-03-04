package ar.utn.ba.ddsi.controllers;

import ar.utn.ba.ddsi.models.dtos.output.GeoSugerenciaDTO;
import ar.utn.ba.ddsi.services.impl.GeoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/geo")
@CrossOrigin(origins = {
    "http://localhost:8080",
    "https://frontend-production-d131.up.railway.app"
})
public class GeoController {

  private final GeoService geoService;

  public GeoController(GeoService geoService) {
    this.geoService = geoService;
  }

  @GetMapping("/sugerencias")
  public List<GeoSugerenciaDTO> sugerencias(
      @RequestParam("query") String query,
      @RequestParam(value = "provinciaId", required = false) String provinciaId,
      @RequestParam(value = "max", defaultValue = "8") int max
  ) {
    return geoService.buscarSugerencias(query, provinciaId, Math.max(1, Math.min(max, 10)));
  }

  @GetMapping("/reverse")
  public GeoSugerenciaDTO reverseGeo(
      @RequestParam("lat") double latitud,
      @RequestParam("lon") double longitud
  ) {
    return geoService.reverseGeocoding(latitud, longitud);
  }
}
