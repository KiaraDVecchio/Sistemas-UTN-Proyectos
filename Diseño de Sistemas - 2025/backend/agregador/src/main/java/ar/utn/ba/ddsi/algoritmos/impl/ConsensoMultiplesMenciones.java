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
public class ConsensoMultiplesMenciones implements IAlgoritmoDeConsenso {

  @Override
  public void calcularConsenso(Coleccion coleccion, Map<Fuente, List<Hecho>> hechosPorFuente) {

    Set<Fuente> fuentesAgregador = hechosPorFuente.keySet();
    if (fuentesAgregador.size() < 2) {
      System.out.println("Menos de dos fuentes en agregador, marcando todo como no consensuado");
      marcarTodosLosHechosComoNoConsensuados(coleccion);
      return;
    }
    System.out.println("Coleccion=" + coleccion.getHandle()
        + " | fuentes agregador=" + fuentesAgregador.size());


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

      // Mapa variante-> set de fuentes que tienen esa variante (variante = un contenido igual según esIgualContenido)
      List<Hecho> todasLasVariantes = new ArrayList<>();
      for (Fuente f : fuentesAgregador) {
        List<Hecho> lista = hechosPorFuente.getOrDefault(f, Collections.emptyList());
        // todas las variantes con ese título en esa fuente
        List<Hecho> variantesEnFuente = lista.stream()
            .filter(h -> !h.getFueEliminado() && titulo.equals(h.getTitulo()))
            .collect(Collectors.toList());
        todasLasVariantes.addAll(variantesEnFuente);
      }


      List<List<Hecho>> gruposPorContenido = new ArrayList<>();
      for (Hecho v : todasLasVariantes) {
        boolean puesto = false;
        for (List<Hecho> grupo : gruposPorContenido) {
          if (!grupo.isEmpty() && grupo.get(0).esIgualContenido(v)) {
            grupo.add(v);
            puesto = true;
            break;
          }
        }
        if (!puesto) {
          List<Hecho> nuevo = new ArrayList<>();
          nuevo.add(v);
          gruposPorContenido.add(nuevo);
        }
      }

      // Conteos por variante
      List<Integer> counts = gruposPorContenido.stream()
          .map(List::size).collect(Collectors.toList());
      System.out.println(titulo + "' variantes=" + gruposPorContenido.size()
          + " | counts=" + counts);


      boolean hayConsenso = (gruposPorContenido.size() == 1) && (!gruposPorContenido.isEmpty())
          && (gruposPorContenido.get(0).size() >= 2);


      for (Hecho h : candidatos) {
        h.setConsensoParaAlgoritmo(TipoAlgoritmoDeConsenso.MULTIPLES_MENCIONES, hayConsenso);
      }
    }
  }

  private void marcarTodosLosHechosComoNoConsensuados(Coleccion coleccion) {
    coleccion.getHechos()
        .forEach(h -> h.setConsensoParaAlgoritmo(TipoAlgoritmoDeConsenso.MULTIPLES_MENCIONES, false));
  }

  @Override
  public TipoAlgoritmoDeConsenso getTipo() {
    return TipoAlgoritmoDeConsenso.MULTIPLES_MENCIONES;
  }
}

