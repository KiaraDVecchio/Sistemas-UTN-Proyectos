package ar.utn.ba.ddsi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GeoConfig {
  @Bean(name = "georefWebClient")
  public WebClient georefWebClient() {
    return WebClient.builder()
        .baseUrl("https://apis.datos.gob.ar/georef/api")
        .build();
  }
}
