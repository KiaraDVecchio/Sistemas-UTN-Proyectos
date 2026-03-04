package ar.utn.ba.ddsi.Metamapa.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudEdicionCreacionDTO {

  private Long id;
  private String tipoSolicitud;
  private String estado;
  private LocalDate fechaSolicitud;
  private String comentario;

  private HechoDTO hecho;

  private Long idContribuyente;
  private String nombreContribuyente;
}