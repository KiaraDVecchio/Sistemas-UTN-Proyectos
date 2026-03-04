package ar.utn.ba.ddsi.services;

import java.util.Optional;
import java.util.Set;

public interface IRaeService {
  Optional<String> lemaConAcento(String termino);
  Set<String> sinonimos(String termino);
}
