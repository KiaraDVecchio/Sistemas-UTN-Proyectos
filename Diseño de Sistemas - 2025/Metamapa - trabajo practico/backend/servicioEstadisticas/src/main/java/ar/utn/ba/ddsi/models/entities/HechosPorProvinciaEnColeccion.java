package ar.utn.ba.ddsi.models.entities;

import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import java.util.LinkedHashMap;
import java.util.Map;

@Embeddable
@Getter @Setter @NoArgsConstructor
public class HechosPorProvinciaEnColeccion {

  @Column(name = "hechosPorProvincia_coleccion_handle")
  private String coleccionHandle;

  @Column(name = "hechos_provincia_ganadora")
  private String provinciaGanadora;

  @Column(name = "hechosPorProvincia_cantidad_ganadora")
  private Long cantidadGanadora;

  @ElementCollection
  @CollectionTable(
      name = "hechos_cant_por_provincia",
      joinColumns = @JoinColumn(name = "estadistica_id")
  )
  @MapKeyColumn(name = "provincia")
  @Column(name = "cantidad")
  private Map<String, Long> cantidadPorProvincia = new LinkedHashMap<>();
}
