package ar.utn.ba.ddsi.models.entities.criterios;

import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.enumerados.Origen;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("origen")
@NoArgsConstructor
@Getter
@Setter
public class CriterioOrigen extends Criterio{

    @Enumerated(EnumType.STRING)
    @Column(name = "origen", nullable = true)
  private Origen origen;

  public CriterioOrigen(Origen origen) {
    this.origen = origen;
  }

  @Override
  public boolean cumpleCriterio(Hecho hecho) {

    return origen.equals(hecho.getOrigen());
  }
}
