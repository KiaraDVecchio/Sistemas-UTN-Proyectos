package ar.utn.ba.ddsi.models.entities.criterios;

import ar.utn.ba.ddsi.models.entities.Hecho;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("titulo")
@NoArgsConstructor
@Getter
@Setter
public class CriterioTitulo extends Criterio{
  @Column(name = "titulo", nullable = true)
  private String titulo;

    public CriterioTitulo(String titulo) {
        this.titulo = titulo;
    }

    @Override
    public boolean cumpleCriterio(Hecho hecho){
        if (hecho.getTitulo() == null) return false;
        return hecho.getTitulo().toLowerCase().contains(titulo.toLowerCase());
    }
}
