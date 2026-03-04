package ar.utn.ba.ddsi.algoritmos.impl;

import ar.utn.ba.ddsi.algoritmos.IAlgoritmoDeConsenso;
import ar.utn.ba.ddsi.models.entities.Coleccion;
import ar.utn.ba.ddsi.models.entities.Fuente;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoAlgoritmoDeConsenso;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ConsensoMayoriaSimple implements IAlgoritmoDeConsenso {

  @Override
  public void calcularConsenso(Coleccion coleccion, Map<Fuente, List<Hecho>> hechosPorFuente) {

    Set<Fuente> fuentesAgregador = hechosPorFuente.keySet();
    if (fuentesAgregador.isEmpty()) {
      marcarTodosLosHechosComoNoConsensuados(coleccion);
      return;
    }

    int totalFuentes = fuentesAgregador.size();
    int mayoriaRequerida = (totalFuentes + 1) / 2; // ceil(N/2)

    System.out.println("Colecion=" + coleccion.getHandle());
    System.out.println("Total fuentes agregador: " + totalFuentes + ", mayoria requerida: " + mayoriaRequerida);


    Map<String, List<Hecho>> candidatosPorTitulo = new HashMap<>();
    if (coleccion.getHechos() != null) {
      for (Hecho h : coleccion.getHechos()) {
        if (!h.getFueEliminado()) {
          candidatosPorTitulo.computeIfAbsent(h.getTitulo(), k -> new ArrayList<>()).add(h);
        }
      }
    }
    System.out.println("titulos candidatos: " + candidatosPorTitulo.keySet());


    for (Map.Entry<String, List<Hecho>> entry : candidatosPorTitulo.entrySet()) {
      String titulo = entry.getKey();
      List<Hecho> candidatos = entry.getValue();

      Map<Hecho, Integer> conteo = contarHechosPorContenido(titulo, fuentesAgregador, hechosPorFuente);

      List<Integer> counts = new ArrayList<>(conteo.values());
      System.out.println("Conteo por variante par '" + titulo + "': " + counts);

      boolean hayMayoria = conteo.values().stream().anyMatch(cnt -> cnt >= mayoriaRequerida);

      if (hayMayoria) {
        marcarHechosConMayoria(candidatos, conteo, mayoriaRequerida);
      } else {
        candidatos.forEach(h -> h.setConsensoParaAlgoritmo(TipoAlgoritmoDeConsenso.MAYORIA_SIMPLE, false));
      }
    }
  }


  private Map<Hecho, Integer> contarHechosPorContenido(
      String titulo,
      Set<Fuente> fuentesAgregador,
      Map<Fuente, List<Hecho>> hechosPorFuente
  ) {
    Map<Hecho, Integer> conteo = new HashMap<>();

    for (Fuente fuente : fuentesAgregador) {
      List<Hecho> hechosDeFuente = hechosPorFuente.getOrDefault(fuente, Collections.emptyList());

      List<Hecho> variantes = hechosDeFuente.stream()
          .filter(h -> !h.getFueEliminado() && titulo.equals(h.getTitulo()))
          .collect(Collectors.toList());

      for (Hecho variante : variantes) {
        Hecho existente = conteo.keySet().stream()
            .filter(h -> h.esIgualContenido(variante))
            .findFirst()
            .orElse(null);

        if (existente != null) {
          conteo.put(existente, conteo.get(existente) + 1);
        } else {
          conteo.put(variante, 1);
        }
      }
    }
    return conteo;
  }

  private void marcarHechosConMayoria(List<Hecho> hechos, Map<Hecho, Integer> conteo, int mayoriaRequerida) {
    for (Hecho h : hechos) {
      boolean tieneConsenso = conteo.entrySet().stream()
          .anyMatch(e -> e.getKey().esIgualContenido(h) && e.getValue() >= mayoriaRequerida);
      h.setConsensoParaAlgoritmo(TipoAlgoritmoDeConsenso.MAYORIA_SIMPLE, tieneConsenso);
    }
  }

  private void marcarTodosLosHechosComoNoConsensuados(Coleccion coleccion) {
    coleccion.getHechos().forEach(h -> h.setConsensoParaAlgoritmo(TipoAlgoritmoDeConsenso.MAYORIA_SIMPLE, false));
  }

  @Override
  public TipoAlgoritmoDeConsenso getTipo() {
    return TipoAlgoritmoDeConsenso.MAYORIA_SIMPLE;
  }
}


