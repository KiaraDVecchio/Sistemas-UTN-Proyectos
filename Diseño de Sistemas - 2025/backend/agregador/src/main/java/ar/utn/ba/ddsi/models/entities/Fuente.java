package ar.utn.ba.ddsi.models.entities;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoFuente;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fuente")
@Getter
@Setter
@NoArgsConstructor
public class Fuente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoFuente tipo;

    public Fuente(String url, TipoFuente tipo) {
        this.url = url;
        this.tipo = tipo;
    }
}

