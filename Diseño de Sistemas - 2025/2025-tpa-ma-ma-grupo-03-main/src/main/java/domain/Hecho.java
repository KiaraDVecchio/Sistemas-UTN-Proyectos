package domain;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import domain.criterios.Criterio;
import domain.enumerados.Origen;
import lombok.Getter;
import lombok.Setter;

public class Hecho {
  @Getter private String titulo;
  @Getter private String descripcion;
  @Getter private Categoria categoria;
  @Getter private Ubicacion ubicacion;
  @Getter private LocalDate fechaAcontecimiento;
  @Getter private LocalDate fechaCarga;
  @Getter private Origen origen;
  @Setter @Getter private boolean fueEliminado;
  @Setter @Getter private Set<Etiqueta> etiquetas;

  //  @Setter @Getter private String nombreAportante;
  //  @Setter @Getter private String apellidoAportante;
  //  @Setter @Getter private Integer edadAportante;

  public Hecho(String titulo, String descripcion, Categoria categoria, Ubicacion ubicacion, LocalDate fechaAcontecimiento, Origen origen){
    this.titulo = titulo;
    this.descripcion = descripcion;
    this.categoria = categoria;
    this.ubicacion = ubicacion;
    this.fechaAcontecimiento = fechaAcontecimiento;
    this.fechaCarga = LocalDate.now();
    this.origen = origen;
    this.fueEliminado = false;
    this.etiquetas = new HashSet<>();
  }

  public void agregarEtiqueta(Etiqueta etiqueta) {
    this.etiquetas.add(etiqueta);
  }

}
