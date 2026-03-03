package domain;

import domain.criterios.Criterio;
import domain.fuentes.Fuente;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import domain.fuentes.FuenteEstatica;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;

public class Coleccion {
  @Getter private String titulo;
  @Getter private String descripcion;
  @Getter private Fuente fuente;
  @Getter private Set<Criterio> criterios;
  @Getter private List<Hecho> hechosDeLaColeccion;

  public Coleccion(String titulo, String descripcion, Fuente fuente){
    this.titulo = titulo;
    this.descripcion = descripcion;
    this.fuente = fuente;
    this.criterios = new HashSet<>();
    this.hechosDeLaColeccion = new ArrayList<>();
  }

  public void filtrarHechos(){
    this.hechosDeLaColeccion.clear();
    List<Hecho> hechosFiltrados = fuente.getHechos().stream().filter(this::noFueEliminado).collect(Collectors.toList());
    if(this.criterios.isEmpty()){
      this.agregarHechos(hechosFiltrados);}
    else {this.agregarHechos(hechosFiltrados.stream().filter(this::cumpleLosCriterios).collect(Collectors.toList()));}
    }

  public void agregarHechos(List<Hecho> hechos){
    this.hechosDeLaColeccion.addAll(hechos);
  }

  public boolean cumpleLosCriterios(Hecho hecho){
    return this.criterios.stream().allMatch(c -> c.cumpleCriterio(hecho));
  }

  public boolean noFueEliminado(Hecho hecho){
    return !hecho.isFueEliminado();
  }

  public void agregarCriterios(Criterio ... nuevosCriterios){
    Collections.addAll(criterios, nuevosCriterios);
  }

}



