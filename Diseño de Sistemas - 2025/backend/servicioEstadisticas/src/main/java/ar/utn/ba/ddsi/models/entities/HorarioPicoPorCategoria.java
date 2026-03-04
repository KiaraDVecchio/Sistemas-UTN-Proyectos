package ar.utn.ba.ddsi.models.entities;

import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;
import java.util.LinkedHashMap;
import java.util.Map;

@Embeddable
@Getter @Setter @NoArgsConstructor
public class HorarioPicoPorCategoria {

  @Column(name = "horarioPico_categoria")
  private String categoria;

  @Column(name = "horarioPico_ganador")
  private Integer horaGanadora;

  @Column(name = "horarioPico_ganadorCantidad")
  private Long cantidadGanadora;

  @ElementCollection
  @CollectionTable(
      name = "horarioPico_cant_por_hora",
      joinColumns = @JoinColumn(name = "estadistica_id")
  )
  @MapKeyColumn(name = "hora")
  @Column(name = "cantidad")
  private Map<Integer, Long> cantidadPorHora = new LinkedHashMap<>();
}
