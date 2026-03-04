package ar.utn.ba.ddsi.models.dtos.input;

import ar.utn.ba.ddsi.models.entities.Coleccion;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoAlgoritmoDeConsenso;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class ColeccionInputDTO {
  private String titulo;
  private String descripcion;
  private String handle;
  private TipoAlgoritmoDeConsenso algoritmoDeConsenso;
  private Set<Long> fuenteIds;
  private Set<Long> criterioIds;
  private List<CriterioInputDTO> nuevosCriterios = new ArrayList<>();
  private Integer cantVistas;
}
