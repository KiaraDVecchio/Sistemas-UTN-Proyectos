package ar.utn.ba.ddsi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class ApiMetaMapaConfig {

  @Bean(name="apiMetaMapaClient")
  public WebClient apiClient(WebClient.Builder builder) {
    return builder
        .baseUrl("https://0c8afd58-91d2-4a96-bfdb-c57ff73160dd.mock.pstmn.io").build(); //URL del mock

  }
}


