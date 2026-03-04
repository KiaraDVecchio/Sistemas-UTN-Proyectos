package ar.utn.ba.ddsi.Metamapa.controllers;

import ar.utn.ba.ddsi.Metamapa.models.dtos.ColeccionDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoDTO;
import ar.utn.ba.ddsi.Metamapa.services.ColeccionService;
import ar.utn.ba.ddsi.Metamapa.services.HechoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class LandingController {

    private final HechoService hechoService;
    private final ColeccionService coleccionService;

  @GetMapping("/inicio")
  public String inicio(Model model){
      List<HechoDTO> hechos = this.hechoService.getUltimosHechos();
    List<ColeccionDTO> coleccionesDestacadas = this.coleccionService.traerColeccionesDestacadas();
    List<HechoDTO> hechosDestacados = this.hechoService.traerHechosDestacados();
    model.addAttribute("titulo", "Inicio");
    model.addAttribute("coleccionesDestacadas", coleccionesDestacadas);
    model.addAttribute("hechosDestacados", hechosDestacados);
    model.addAttribute("hechos", hechos);
    return "landing/landing";
  }

}
