package ar.utn.ba.ddsi.models.dtos.input;

import ar.utn.ba.ddsi.models.entities.enumerados.EstadoSolicitud;
import lombok.Data;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Data
public class SolicitudInputDTO {
    private Long hechoId;
    private String justificacion;
    private EstadoSolicitud estado;
    private String administradorQueAtendio;
    private LocalDateTime fechaEntrada;
    private LocalDateTime fechaAtencion;
}
