package ar.utn.ba.ddsi.services.impl;

import ar.utn.ba.ddsi.normalizadores.Normalizador;
import ar.utn.ba.ddsi.normalizadores.RaeClient;
import ar.utn.ba.ddsi.services.IRaeService;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RaeService implements IRaeService {
  private final RaeClient client;
  private final Normalizador normalizador;

  public RaeService(RaeClient client, Normalizador normalizador) {
    this.client = client;
    this.normalizador = normalizador;
  }

  @Override
  public Optional<String> lemaConAcento(String termino) {
    return client.getWord(termino).map(w -> w.data != null ? w.data.word : null);
  }

  @Override
  public Set<String> sinonimos(String termino) {
    return client.getWord(termino)
        .map(w -> {
          Set<String> out = new HashSet<>();
          if (w.data != null && w.data.meanings != null) {
            for (var m : w.data.meanings) {
              if (m.senses != null) {
                for (var s : m.senses) {
                  if (s.synonyms != null) out.addAll(s.synonyms);
                }
              }
            }
          }
          // normalizá para comparar con tu DB
          return out.stream().map(normalizador::normalizar).collect(Collectors.toSet());
        })
        .orElseGet(Set::of);
  }
}
