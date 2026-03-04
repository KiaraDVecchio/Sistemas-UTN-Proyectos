package ar.utn.ba.ddsi.models.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor
public class Hecho {
  private String titulo;
  private String descripcion;
  private Categoria categoria;
  private Ubicacion ubicacion;
  private LocalDateTime fechaYHoraAcontecimiento;
  private LocalDate fechaCarga;
  private Boolean fueEliminado;
  private String fuenteExterna;
}
