package ar.utn.ba.ddsi.models.dtos.output;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HechoOutputDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private String categoria;
    private LocalDateTime fechaAcontecimiento;
    private LocalDate fechaCarga;
    private Boolean fueEliminado;
    private Long idEnFuente;
    private Double latitud;
    private Double longitud;
    private String provincia;

    private List<String> idContenidoMultimedia;
}