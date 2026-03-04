package ar.utn.ba.ddsi.services.impl;

import ar.utn.ba.ddsi.models.dtos.input.LoginDTO;
import ar.utn.ba.ddsi.models.dtos.input.LoginResponseDTO;
import ar.utn.ba.ddsi.services.ILoginApiCatedra;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class LoginApiCatedra implements ILoginApiCatedra {

    private final WebClient authClient;
    private String token;


    @Value("${api.catedra.email:ddsi@gmail.com}")
    private String email;

    @Value("${api.catedra.password:ddsi2025*}")
    private String password;

    public LoginApiCatedra(WebClient.Builder webClientBuilder) {
        this.authClient = webClientBuilder
                .baseUrl("https://api-ddsi.disilab.ar/public/api")
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public Mono<LoginResponseDTO> loginAndStore(LoginDTO creds) {
        return realizarLogin(creds);
    }

    @Override
    public synchronized String getToken() {
        if (this.token == null) {
            System.out.println("Autenticando con API Cátedra...");
            try {
                LoginDTO creds = new LoginDTO();
                creds.setEmail(this.email);
                creds.setPassword(this.password);

                LoginResponseDTO resp = realizarLogin(creds).block();

                if (resp != null && resp.getData() != null) {
                    this.token = resp.getData().getAccessToken();
                    System.out.println("Login API Cátedra exitoso.");
                } else {
                    throw new RuntimeException("Respuesta de login vacía");
                }
            } catch (Exception e) {
                System.err.println("Falló el auto-login a la Cátedra: " + e.getMessage());
                throw new RuntimeException("No se pudo autenticar con la API externa", e);
            }
        }
        return this.token;
    }


    private Mono<LoginResponseDTO> realizarLogin(LoginDTO creds) {
        return authClient.post()
                .uri("/login")
                .bodyValue(creds)
                .retrieve()
                .bodyToMono(LoginResponseDTO.class)
                .doOnNext(resp -> {

                    if (resp.getData() != null) {
                        this.token = resp.getData().getAccessToken();
                    }
                });
    }
}