package ar.utn.ba.ddsi.services;
import ar.utn.ba.ddsi.models.dtos.input.HechoInputDTO;
import ar.utn.ba.ddsi.models.dtos.input.HechoInputEdicionDTO;
import ar.utn.ba.ddsi.models.dtos.output.ContribuyenteOutputDTO;
import ar.utn.ba.ddsi.models.dtos.output.HechoOutputDTO;
import ar.utn.ba.ddsi.models.dtos.output.UbicacionOutputDTO;
import ar.utn.ba.ddsi.models.entities.Categoria;
import ar.utn.ba.ddsi.models.entities.Contribuyente;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.Ubicacion;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface IHechoService {
  //HechoOutputDTO crear(HechoInputDTO hechoInputDTO);
  HechoOutputDTO crear(HechoInputDTO hechoInputDTO , MultipartFile[] multimedia);
  HechoOutputDTO hechoOutputDTO(Hecho hecho);
  void eliminar(Long id);
  boolean puedeEditar(Long id1 , Long id2, LocalDate fecha);
  HechoOutputDTO permisoDeEdicion(Long idEditor, Long idHecho);
  //HechoOutputDTO edicion(Long idEditor, HechoInputDTO hechoInputDTO, Long idHecho);
  HechoOutputDTO edicion(Long idEditor, HechoInputEdicionDTO hechoInputDTO, Long idHecho, MultipartFile[] multimedia, boolean replaceMedia, List<String> deleteExisting);
  void creacionRechazada(Hecho hecho);
  void edicionRechazada(Hecho hecho);
  List<HechoOutputDTO> buscarTodos();
  void actualizarHecho(Hecho hecho, String titulo, String descripcion, Categoria categoria, Ubicacion ubicacion, LocalDateTime fechaAcontecimiento);
  UbicacionOutputDTO ubicacionOutputDTO(Ubicacion ubicacion);
  ContribuyenteOutputDTO contribuyenteOutputDTO(Contribuyente contribuyente);
}
