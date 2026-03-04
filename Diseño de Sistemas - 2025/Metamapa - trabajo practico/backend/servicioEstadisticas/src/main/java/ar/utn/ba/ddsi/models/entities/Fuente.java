package ar.utn.ba.ddsi.models.entities;

import ar.utn.ba.ddsi.models.entities.enumerados.TipoFuente;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fuente")
@Getter @Setter @NoArgsConstructor
public class Fuente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="url")
    private String url;

    @Enumerated(EnumType.STRING)
    private TipoFuente tipo;

    public Fuente(String url, TipoFuente tipo) {
        this.url = url;
        this.tipo = tipo;
    }
}
