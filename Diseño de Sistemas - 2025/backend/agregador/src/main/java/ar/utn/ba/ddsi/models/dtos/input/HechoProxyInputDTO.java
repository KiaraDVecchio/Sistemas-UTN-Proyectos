package ar.utn.ba.ddsi.models.dtos.input;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HechoProxyInputDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private String categoria;

    @JsonAlias({"fechaAcontecimiento", "fecha_hecho", "fechaHecho"})
    private LocalDate fechaAcontecimiento;

    private String provincia;
    private Double latitud;
    private Double longitud;
}