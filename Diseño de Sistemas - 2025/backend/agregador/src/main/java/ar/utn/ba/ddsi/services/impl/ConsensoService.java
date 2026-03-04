package ar.utn.ba.ddsi.services.impl;

import ar.utn.ba.ddsi.algoritmos.impl.AlgoritmoDeConsensoFactory;
import ar.utn.ba.ddsi.algoritmos.IAlgoritmoDeConsenso;
import ar.utn.ba.ddsi.models.entities.Coleccion;
import ar.utn.ba.ddsi.models.entities.Fuente;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.repositories.IColeccionRepository;
import ar.utn.ba.ddsi.models.repositories.IFuenteRepository;
import ar.utn.ba.ddsi.services.IAgregadorService;
import ar.utn.ba.ddsi.services.IConsensoService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConsensoService implements IConsensoService {
  @Autowired
  private IAgregadorService agregadorService;
  @Autowired
  private IColeccionRepository coleccionRepository;
  @Autowired
  private IFuenteRepository fuenteRepository;
  @Autowired
  private AlgoritmoDeConsensoFactory algoritmoFactory;

  @Override
  @Transactional
  public void aplicarAlgoritmoDeConsenso() {
    List<Coleccion> colecciones = coleccionRepository.findAllWithHechos();

    List<Coleccion> coleccionesConAlgoritmo = colecciones.stream()
        .filter(coleccion -> coleccion.getAlgoritmoDeConsenso() != null)
        .toList();

    this.reiniciarConsenso(coleccionesConAlgoritmo);

    Map<Fuente, List<Hecho>> hechosPorFuente = new HashMap<>();

    List<Fuente> todasLasFuentes = fuenteRepository.findAll();

    for (Fuente fuente : todasLasFuentes) {
      hechosPorFuente.put(fuente, agregadorService.obtenerTodosLosHechosDeFuente(fuente));
    }

    coleccionesConAlgoritmo.forEach(coleccion -> this.usoDeAlgoritmo(coleccion, hechosPorFuente));

    colecciones.forEach(coleccion -> coleccionRepository.save(coleccion));
  }


  @Override
  public void usoDeAlgoritmo(Coleccion coleccion, Map<Fuente, List<Hecho>> hechosPorFuente) {
    IAlgoritmoDeConsenso algoritmo = algoritmoFactory.resolver(coleccion.getAlgoritmoDeConsenso());
    if (algoritmo != null) {
      algoritmo.calcularConsenso(coleccion, hechosPorFuente);
    }else {
      System.out.println("Algoritmo desconocido");
    }
  }

  private void reiniciarConsenso(List<Coleccion> coleccionesConAlgoritmo) {
    coleccionesConAlgoritmo.forEach(coleccion -> {
      coleccion.getHechos().forEach(Hecho::limpiarConsensos);
    });
  }

}