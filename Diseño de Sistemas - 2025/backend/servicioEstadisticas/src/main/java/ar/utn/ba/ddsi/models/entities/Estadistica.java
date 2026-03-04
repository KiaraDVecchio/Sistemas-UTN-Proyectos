package ar.utn.ba.ddsi.models.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "estadistica",
    indexes = { //para facilitar la busqueda desde la bdd, para hacer que en el controller nos pidan directo algunas estats especif
        @Index(name = "idx_estadistica_fecha", columnList = "fecha_de_calculo"),
        @Index(name = "idx_estadistica_coleccion", columnList = "hechosPorProvincia_coleccion_handle"),
        @Index(name = "idx_estadistica_categoria1", columnList = "provinciaTopPor_categoria"),
        @Index(name = "idx_estadistica_categoria2", columnList = "horarioPico_categoria")
    })
public class Estadistica {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "fecha_de_calculo", nullable = false)
  private LocalDateTime fechaDeCalculo;

  @Embedded
  private HechosPorProvinciaEnColeccion hechosPorProvinciaEnColeccion;

  @Embedded
  private CategoriaTopGlobal categoriaTopGlobal;

  @Embedded
  private ProvinciaTopPorCategoria provinciaTopPorCategoria;

  @Embedded
  private HorarioPicoPorCategoria horarioPicoPorCategoria;

  @Embedded
  private SolicitudesEliminacionSpam solicitudesEliminacionSpam;

    @Column(name = "total_hechos")
    private Long totalHechos;

    @Column(name = "hechos_verificados")
    private Long hechosVerificados;

}

