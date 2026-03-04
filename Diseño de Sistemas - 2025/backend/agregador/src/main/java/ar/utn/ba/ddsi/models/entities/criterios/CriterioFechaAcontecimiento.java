package ar.utn.ba.ddsi.models.entities.criterios;

import ar.utn.ba.ddsi.models.entities.Hecho;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@DiscriminatorValue("fecha_acontecimiento")
@NoArgsConstructor
@Getter
@Setter
public class CriterioFechaAcontecimiento extends Criterio{

  @Column(name = "fecha_acontecimiento_desde", nullable = true)
  private LocalDateTime desde;

  @Column(name = "fecha_acontecimiento_hasta", nullable = true)
  private LocalDateTime hasta;

  public CriterioFechaAcontecimiento(LocalDateTime desde, LocalDateTime hasta){
    this.desde = desde;
    this.hasta = hasta;
  }

    @Override
    public boolean cumpleCriterio(Hecho hecho) {
        if (hecho.getFechaAcontecimiento() == null) return false;

        LocalDate fechaHecho = hecho.getFechaAcontecimiento().toLocalDate();

        LocalDate fechaDesde = this.desde.toLocalDate();
        LocalDate fechaHasta = this.hasta.toLocalDate();


        boolean pasoInicio = !fechaHecho.isBefore(fechaDesde);
        boolean pasoFin = !fechaHecho.isAfter(fechaHasta);

        return pasoInicio && pasoFin;
    }
}
