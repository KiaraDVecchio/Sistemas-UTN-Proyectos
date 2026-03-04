package ar.utn.ba.ddsi.models.entities;

import ar.utn.ba.ddsi.algoritmos.IAlgoritmoDeConsenso;
import ar.utn.ba.ddsi.models.entities.criterios.Criterio;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoAlgoritmoDeConsenso;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.ArrayList;
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "coleccion")
public class Coleccion {

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
     private String descripcion;


  @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(
      name = "coleccion_fuente",
      joinColumns = @JoinColumn(name = "coleccion_handle", referencedColumnName = "handle"),
      inverseJoinColumns = @JoinColumn(name = "fuente_id", referencedColumnName = "id")
  )
    private Set<Fuente> fuentes;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "coleccion_handle", referencedColumnName = "handle", nullable = false)
    private Set<Criterio> criterios = new HashSet<>();

     @ManyToMany
     @JoinTable(
         name = "coleccion_hecho",
         joinColumns = @JoinColumn(name = "coleccion_handle", referencedColumnName = "handle"),
         inverseJoinColumns = @JoinColumn(name = "hecho_id")
     )
     private List<Hecho> hechos;

     @Id
     @Column(name = "handle")
     private String handle;

     @Enumerated(EnumType.STRING)
     @Column(name = "algoritmo_de_consenso")
     private TipoAlgoritmoDeConsenso algoritmoDeConsenso;

     @Column(name = "cantidad_vistas")
     private Integer cantVistas;

  public Coleccion(String titulo, String descripcion, Set<Fuente> fuentes) {
    this.titulo = titulo;
    this.descripcion = descripcion;
    this.fuentes = (fuentes != null ? fuentes : new HashSet<>());
    this.criterios = new HashSet<>();
    this.hechos = new ArrayList<>();
    this.algoritmoDeConsenso = null;
  }

  public void agregarFuentes(Fuente ... nuevasFuentes){
    Collections.addAll(fuentes, nuevasFuentes);
  }

  public void agregarHechos(List<Hecho> hechos){
    this.hechos.addAll(hechos);
  }

  public boolean cumpleLosCriterios(Hecho hecho){
    return this.criterios.stream().allMatch(c -> c.cumpleCriterio(hecho));
  }

  public boolean noFueEliminado(Hecho hecho){
    return !hecho.getFueEliminado();
  }

  public void agregarCriterios(Criterio ... nuevosCriterios){
    Collections.addAll(criterios, nuevosCriterios);
  }
}



