package ar.utn.ba.ddsi.models.entities.criterios;

import ar.utn.ba.ddsi.models.entities.Hecho;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("descripcion")
@NoArgsConstructor
@Getter
@Setter
public class CriterioDescripcion extends Criterio{
  @Column(name = "descripcion", nullable = true)
  private String descripcion;

  public CriterioDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

    @Override
    public boolean cumpleCriterio(Hecho hecho){
        if (hecho.getDescripcion() == null) return false;
        return hecho.getDescripcion().toLowerCase().contains(descripcion.toLowerCase());
    }
}
