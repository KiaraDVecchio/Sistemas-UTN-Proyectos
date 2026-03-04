package ar.utn.ba.ddsi.models.entities;

import jakarta.persistence.*;
import lombok.Getter; import lombok.NoArgsConstructor; import lombok.Setter;

@Embeddable
@Getter @Setter @NoArgsConstructor
public class SolicitudesEliminacionSpam {

  @Column(name = "solicitudes_total")
  private Long total;

  @Column(name = "solicitudes_spam")
  private Long spam;
}
