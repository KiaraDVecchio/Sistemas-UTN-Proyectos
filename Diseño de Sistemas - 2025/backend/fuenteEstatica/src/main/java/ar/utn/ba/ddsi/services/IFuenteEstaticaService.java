package ar.utn.ba.ddsi.services;

import ar.utn.ba.ddsi.models.dtos.output.HechoOutputEstaticaDTO;
import ar.utn.ba.ddsi.models.dtos.output.RutaOutputDTO;
import ar.utn.ba.ddsi.models.dtos.output.UbicacionOutputDTO;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.InformeDeResultados;
import ar.utn.ba.ddsi.models.entities.Ruta;
import ar.utn.ba.ddsi.models.entities.Ubicacion;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface IFuenteEstaticaService {
  void leerHechos(Long idRuta);
  void leerTodosLosArchivos();
  List<HechoOutputEstaticaDTO> buscarTodos();
  HechoOutputEstaticaDTO hechoOutputEstaticaDTO(Hecho hecho);
  UbicacionOutputDTO ubicacionOutputDTO(Ubicacion ubicacion);
  RutaOutputDTO crearRuta(String nombre, String path);
  InformeDeResultados procesarCsv(MultipartFile file);
}
