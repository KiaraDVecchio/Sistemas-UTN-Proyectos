package ar.utn.ba.ddsi.Metamapa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Value("${backend.api.base-url}")
    private String adminUrl;

    @Value("${backend.api.base-url-agregador}")
    private String publicUrl;

    private final ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
            .build();

    @Bean("webClientPublic")
    public WebClient webClientPublic() {
        return WebClient.builder()
                .baseUrl(publicUrl)
                .exchangeStrategies(strategies)
                .build();
    }

    @Bean("webClientAdmin")
    public WebClient webClientAdmin() {
        return WebClient.builder()
                .baseUrl(adminUrl)
                .exchangeStrategies(strategies)
                .build();
    }
}