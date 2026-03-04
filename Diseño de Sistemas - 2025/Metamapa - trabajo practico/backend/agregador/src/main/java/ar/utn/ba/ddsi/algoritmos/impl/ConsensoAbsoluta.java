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
public class ConsensoAbsoluta implements IAlgoritmoDeConsenso {

  @Override
  public void calcularConsenso(Coleccion coleccion, Map<Fuente, List<Hecho>> hechosPorFuente) {

    Set<Fuente> fuentesAgregador = hechosPorFuente.keySet();
    int totalFuentes = fuentesAgregador.size();
    if (totalFuentes == 0) {
      marcarTodosLosHechosComoNoConsensuados(coleccion);
      return;
    }

    System.out.println("Coleccion=" + coleccion.getHandle() + " | fuentes agregador=" + totalFuentes);


    Map<String, List<Hecho>> candidatosPorTitulo = new HashMap<>();
    if (coleccion.getHechos() != null) {
      for (Hecho h : coleccion.getHechos()) {
        if (!h.getFueEliminado()) {
          candidatosPorTitulo.computeIfAbsent(h.getTitulo(), k -> new ArrayList<>()).add(h);
        }
      }
    }
    System.out.println("Titulos candidatos: " + candidatosPorTitulo.keySet());


    for (Map.Entry<String, List<Hecho>> entry : candidatosPorTitulo.entrySet()) {
      String titulo = entry.getKey();
      List<Hecho> candidatos = entry.getValue();

      List<Hecho> todasLasVariantes = new ArrayList<>();
      int fuentesConTitulo = 0;

      for (Fuente f : fuentesAgregador) {
        List<Hecho> lista = hechosPorFuente.getOrDefault(f, Collections.emptyList());
        List<Hecho> variantesEnFuente = lista.stream()
            .filter(h -> !h.getFueEliminado() && titulo.equals(h.getTitulo()))
            .collect(Collectors.toList());
        if (!variantesEnFuente.isEmpty()) {
          fuentesConTitulo++;
          todasLasVariantes.addAll(variantesEnFuente);
        }
      }


      List<List<Hecho>> gruposPorContenido = new ArrayList<>();
      for (Hecho v : todasLasVariantes) {
        boolean agregado = false;
        for (List<Hecho> grupo : gruposPorContenido) {
          if (!grupo.isEmpty() && grupo.get(0).esIgualContenido(v)) {
            grupo.add(v);
            agregado = true;
            break;
          }
        }
        if (!agregado) {
          List<Hecho> nuevo = new ArrayList<>();
          nuevo.add(v);
          gruposPorContenido.add(nuevo);
        }
      }

      List<Integer> counts = gruposPorContenido.stream().map(List::size).collect(Collectors.toList());
      System.out.println(titulo + " fuentesConTitulo=" + fuentesConTitulo
          + " | variantes=" + gruposPorContenido.size() + " | counts=" + counts);


      boolean hayConsenso = (fuentesConTitulo == totalFuentes)
          && (gruposPorContenido.size() == 1)
          && (!gruposPorContenido.isEmpty() && gruposPorContenido.get(0).size() == totalFuentes);

      for (Hecho h : candidatos) {
        h.setConsensoParaAlgoritmo(TipoAlgoritmoDeConsenso.CONSENSO_ABSOLUTO, hayConsenso);
      }
    }
  }

  private void marcarTodosLosHechosComoNoConsensuados(Coleccion coleccion) {
    coleccion.getHechos()
        .forEach(h -> h.setConsensoParaAlgoritmo(TipoAlgoritmoDeConsenso.CONSENSO_ABSOLUTO, false));
  }

  @Override
  public TipoAlgoritmoDeConsenso getTipo() {
    return TipoAlgoritmoDeConsenso.CONSENSO_ABSOLUTO;
  }
}

