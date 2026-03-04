package ar.utn.ba.ddsi.models.dtos.output;

import ar.utn.ba.ddsi.models.entities.*;
import ar.utn.ba.ddsi.models.entities.enumerados.Origen;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class HechoOutputDTO {
  private Long id;
  private String titulo;
  private String descripcion;
  private String categoria;
  private Double latitud;
  private Double longitud;
  private String provincia;
  private LocalDateTime fechaAcontecimiento;
  private LocalDate fechaCarga;
  private Set<Long> idEtiquetas;
  private List<String> idContenidoMultimedia;
  private String fuenteExterna;
  private ContribuyenteDTO contribuyente;
  private Boolean fueEliminado;
  private Integer cantVistas;
  private Long idEnFuente;
  private Boolean consensuado;
  private boolean editable;    // 7 días desde fechaCarga
  private int diasRestantes;
  //private int diasRestantesEdicion;
  private boolean tieneEdicionPendiente;

  public static HechoOutputDTO fromEntity(Hecho h) {
    HechoOutputDTO dto = new HechoOutputDTO();
    dto.setId(h.getId());
    dto.setTitulo(h.getTitulo());
    dto.setDescripcion(h.getDescripcion());
    dto.setFueEliminado(h.getFueEliminado());
    dto.setIdEnFuente(h.getIdEnFuente());
    dto.setCategoria(h.getCategoria() != null ? h.getCategoria().getNombre() : null);
    if (h.getUbicacion() != null) {
      dto.setLatitud(h.getUbicacion().getLatitud());
      dto.setLongitud(h.getUbicacion().getLongitud());
      dto.setProvincia(h.getUbicacion().getProvincia());
    } else {
      dto.setLatitud(null);
      dto.setLongitud(null);
      dto.setProvincia(null);
    }
    dto.setFechaAcontecimiento(h.getFechaAcontecimiento());
    dto.setFechaCarga(h.getFechaCarga());
    dto.setIdEtiquetas(
        h.getEtiquetas() != null
            ? h.getEtiquetas().stream().map(Etiqueta::getId).collect(java.util.stream.Collectors.toSet())
            : java.util.Set.of()
    );
    dto.setIdContenidoMultimedia(
        h.getPathMultimedia() != null ? h.getPathMultimedia() : java.util.List.of());
    dto.setFuenteExterna(h.getFuenteExterna());
    return dto;
  }


}