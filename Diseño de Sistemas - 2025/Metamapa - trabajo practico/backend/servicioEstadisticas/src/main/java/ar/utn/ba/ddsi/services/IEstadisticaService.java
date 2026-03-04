package ar.utn.ba.ddsi.services;

import ar.utn.ba.ddsi.models.dtos.output.CategoriaOutputDTO;
import ar.utn.ba.ddsi.models.dtos.output.HoraOutputDTO;
import ar.utn.ba.ddsi.models.dtos.output.ProvinciaOutputDTO;
import ar.utn.ba.ddsi.models.entities.Hecho;
import java.util.List;
import java.util.Set;

public interface IEstadisticaService {
  void recalcularEstadisticas();
}
