package ar.utn.ba.ddsi.Metamapa.models.dtos;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class SolicitudDTO {

    private Long id;
    private String estado;
    private String justificacion;
    private LocalDateTime fechaEntrada;
    private LocalDateTime fechaAtencion;
    private Long hechoId;
    private Boolean esSpam;
}
