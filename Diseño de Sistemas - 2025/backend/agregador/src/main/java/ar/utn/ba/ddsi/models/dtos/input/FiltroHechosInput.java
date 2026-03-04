package ar.utn.ba.ddsi.models.dtos.input;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FiltroHechosInput {
    private String categoria;
    private String provincia;
    private String fechaDesde;
    private String fechaHasta;
    private String tituloContiene;

  }


