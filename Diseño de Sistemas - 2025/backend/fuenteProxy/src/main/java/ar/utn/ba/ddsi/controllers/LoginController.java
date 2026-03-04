package ar.utn.ba.ddsi.controllers;

import ar.utn.ba.ddsi.models.dtos.input.LoginDTO;
import ar.utn.ba.ddsi.models.dtos.input.LoginResponseDTO;
import ar.utn.ba.ddsi.services.impl.LoginApiCatedra;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/login")
public class LoginController {

  private final LoginApiCatedra loginApiCatedra;

  public LoginController(LoginApiCatedra loginApiCatedra) {
    this.loginApiCatedra = loginApiCatedra;
  }

  @PostMapping
  public Mono<ResponseEntity<LoginResponseDTO>> login(@RequestBody LoginDTO creds) {
    return loginApiCatedra
        .loginAndStore(creds)
        .map(ResponseEntity::ok)
        .onErrorResume(e ->
            Mono.just(ResponseEntity.status(401).build())
        );
  }
}
