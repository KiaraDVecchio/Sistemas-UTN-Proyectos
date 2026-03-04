package ar.utn.ba.ddsi.normalizadores;


import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;

@Component
public class RaeClient {

  private final WebClient web;
  private final long timeoutMs;

  public RaeClient(WebClient.Builder builder,
                   @Value("${rae.base-url:https://rae-api.com}") String baseUrl,
                   @Value("${rae.timeout-ms:2000}") long timeoutMs) {
    this.web = builder.baseUrl(baseUrl).build();
    this.timeoutMs = timeoutMs;
  }

  public Optional<RaeWord> getWord(String word) {
    try {
      return Optional.ofNullable(
          web.get().uri(uri -> uri.path("/api/words/{w}").build(word))
              .retrieve()
              .bodyToMono(RaeWord.class)
              .timeout(Duration.ofMillis(timeoutMs))
              .block()
      );
    } catch (Exception e) {
      return Optional.empty(); // si falla, degradamos sin romper el flujo
    }
  }

  // DTOS
  public static class RaeWord {
    public Data data;
    public static class Data {
      public String word;   // con tilde
      public List<Meaning> meanings;
    }
    public static class Meaning {
      public List<Sense> senses;
    }
    public static class Sense {
      public List<String> synonyms;
    }
  }
}
