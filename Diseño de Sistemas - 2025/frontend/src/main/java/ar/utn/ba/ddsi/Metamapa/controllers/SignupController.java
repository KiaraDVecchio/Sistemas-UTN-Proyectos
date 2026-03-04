package ar.utn.ba.ddsi.Metamapa.controllers;

import ar.utn.ba.ddsi.Metamapa.models.dtos.RegisterRequestDTO;
import ar.utn.ba.ddsi.Metamapa.services.MetaMapaApiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping
public class SignupController {
  private final MetaMapaApiService metaMapaApiService;

  public SignupController(MetaMapaApiService metaMapaApiService) {
    this.metaMapaApiService = metaMapaApiService;
  }

  @GetMapping("/signup")
  public String signup(Model model){
    model.addAttribute("register", new RegisterRequestDTO());
    return "authentication/signup";
  }

  @PostMapping("/signup")
  public String procesarSignup(@ModelAttribute RegisterRequestDTO dto, RedirectAttributes ra) {

    try {
      metaMapaApiService.signupAndGetTokens(dto);
      ra.addFlashAttribute("mensaje", "Cuenta creada con éxito. Inicia sesión para continuar.");
      return "redirect:/login";
    } catch (Exception e) {
      ra.addFlashAttribute("error", "No se pudo crear la cuenta: " + e.getMessage());
      return "redirect:/signup";
    }
  }


}
