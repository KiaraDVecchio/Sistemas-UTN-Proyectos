package ar.utn.ba.ddsi.Metamapa.models.dtos;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class ColeccionDTO {
    private String titulo;
    private String descripcion;
    private String handle;
    private String algoritmoDeConsenso;

    // IDs (Mantenemos por compatibilidad)
    private Set<Long> fuenteIds;
    private Set<Long> criterioIds;
    private Set<Long> hechoIds;

    private Integer cantVistas;

    private List<FuenteDTO> fuentes = new ArrayList<>();
    private List<CriterioDTO> criterios = new ArrayList<>();

    private List<CriterioDTO> nuevosCriterios = new ArrayList<>();

    public ColeccionDTO() {}

    public ColeccionDTO(String titulo, String descripcion, String handle) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.handle = handle;
    }
}