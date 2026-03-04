package ar.utn.ba.ddsi.services;

import ar.utn.ba.ddsi.models.dtos.input.LoginDTO;
import ar.utn.ba.ddsi.models.dtos.input.LoginResponseDTO;
import reactor.core.publisher.Mono;

public interface ILoginApiCatedra {
  Mono<LoginResponseDTO> loginAndStore(LoginDTO creds);
  String getToken();
}
