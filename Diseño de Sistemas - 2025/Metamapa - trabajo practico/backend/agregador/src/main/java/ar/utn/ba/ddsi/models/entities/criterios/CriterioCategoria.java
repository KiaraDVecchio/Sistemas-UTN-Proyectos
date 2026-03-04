package ar.utn.ba.ddsi.models.entities.criterios;

import ar.utn.ba.ddsi.models.entities.Categoria;
import ar.utn.ba.ddsi.models.entities.Hecho;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("categoria")
@NoArgsConstructor
@Getter
@Setter
public class CriterioCategoria extends Criterio{

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "categoria_id", nullable = true)
  private Categoria categoria;

  public CriterioCategoria(Categoria categoria){
    this.categoria = categoria;
  }

    @Override
    public boolean cumpleCriterio(Hecho hecho) {
        if (hecho.getCategoria() == null || hecho.getCategoria().getNombre() == null) {
            return false;
        }
        return categoria.getNombre().equalsIgnoreCase(hecho.getCategoria().getNombre());
    }

}
