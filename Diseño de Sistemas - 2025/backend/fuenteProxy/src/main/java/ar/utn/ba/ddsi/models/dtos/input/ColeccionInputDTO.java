package ar.utn.ba.ddsi.models.dtos.input;

import ar.utn.ba.ddsi.models.entities.Fuente;
//import ar.utn.ba.ddsi.models.entities.criterios.Criterio;
import lombok.Data;
import lombok.Getter;
import java.util.List;
import java.util.Set;

@Getter
@Data
public class ColeccionInputDTO {
  private String titulo;
  private String descripcion;
  private Fuente fuente;
  //private Set<Criterio> criterios;
  private List<HechoInputDTO> hechosDeLaColeccion;

}
