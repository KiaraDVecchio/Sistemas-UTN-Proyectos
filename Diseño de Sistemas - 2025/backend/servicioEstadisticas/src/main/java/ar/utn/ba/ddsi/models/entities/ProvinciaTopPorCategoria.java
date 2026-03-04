package ar.utn.ba.ddsi.models.entities;

import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import java.util.LinkedHashMap;
import java.util.Map;

@Embeddable
@Getter @Setter @NoArgsConstructor
public class ProvinciaTopPorCategoria {

  @Column(name = "provinciaTopPor_categoria")
  private String categoria;

  @Column(name = "top_provincia_ganadora")
  private String provinciaGanadora;

  @Column(name = "top_cantidad_Provganadora")
  private Long cantidadGanadora;

  @ElementCollection
  @CollectionTable(
      name = "categoria_cant_por_provincia",
      joinColumns = @JoinColumn(name = "estadistica_id")
  )
  @MapKeyColumn(name = "provincia")
  @Column(name = "cantidad")
  private Map<String, Long> cantidadPorProvincia = new LinkedHashMap<>();
}
