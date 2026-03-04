package ar.utn.ba.ddsi.config;

import ar.utn.ba.ddsi.models.entities.Fuente;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoFuente;
import ar.utn.ba.ddsi.models.repositories.IFuenteRepository;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FuenteDataLoader implements CommandLineRunner{
  private final IFuenteRepository fuenteRepository;

  @Override
  public void run(String... args) {

    if (fuenteRepository.count() > 0) {
      return;
    }

    Fuente dinamica = new Fuente();
    dinamica.setTipo(TipoFuente.DINAMICA);
    dinamica.setUrl("https://fuentedinamica-production-b73f.up.railway.app/api/hechos");
    fuenteRepository.save(dinamica);

    Fuente estatica = new Fuente();
    estatica.setTipo(TipoFuente.ESTATICA);
    estatica.setUrl("https://fuenteestatica-production-f039.up.railway.app/api/hechos");
    fuenteRepository.save(estatica);

    Fuente proxy = new Fuente();
    proxy.setTipo(TipoFuente.PROXY);
    proxy.setUrl("https://fuenteproxy-production-8655.up.railway.app/api/hechos");
    fuenteRepository.save(proxy);

    System.out.println("=== Fuentes iniciales creadas ===");
  }
}
