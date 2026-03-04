package ar.utn.ba.ddsi.services;

import ar.utn.ba.ddsi.models.dtos.input.ColeccionInputDTO;
import ar.utn.ba.ddsi.models.dtos.output.ColeccionOutputDTO;
import ar.utn.ba.ddsi.models.entities.Coleccion;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoDeModoNavegacion;
import java.util.List;

public interface IColeccionService {
  public void actualizarColecciones();
  public Coleccion crearColeccion(Coleccion coleccion);
  public List<Hecho> obtenerHechosPorColeccion(String coleccionId, TipoDeModoNavegacion modoNavegacion);
  public Coleccion findById(String id);
  ColeccionOutputDTO crearColeccion(ColeccionInputDTO dto);
    Hecho obtenerHechoPorId(Long id);
    Coleccion filtrarHechos(Coleccion coleccion);
}
