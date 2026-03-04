package ar.utn.ba.ddsi.models.entities;

import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import java.util.LinkedHashMap;
import java.util.Map;

@Embeddable
@Getter @Setter @NoArgsConstructor
public class CategoriaTopGlobal {

  @Column(name = "top_categoria_ganadora")
  private String categoriaGanadora;

  @Column(name = "top_cantidad_Catganadora")
  private Long cantidadGanadora;

  @ElementCollection
  @CollectionTable(
      name = "top_cant_por_categoria",
      joinColumns = @JoinColumn(name = "estadistica_id")
  )
  @MapKeyColumn(name = "categoria")
  @Column(name = "cantidad")
  private Map<String, Long> cantidadPorCategoria = new LinkedHashMap<>(); //Queda mas "lindo" que el hashmap para le dashboard
}
