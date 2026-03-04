package ar.utn.ba.ddsi.services;

import ar.utn.ba.ddsi.models.dtos.input.CategoriaInputDTO;
import ar.utn.ba.ddsi.models.dtos.input.ColeccionInputDTO;
import ar.utn.ba.ddsi.models.dtos.input.FuenteInputDTO;

import ar.utn.ba.ddsi.models.dtos.output.*;
import ar.utn.ba.ddsi.models.entities.Coleccion;
import ar.utn.ba.ddsi.models.entities.enumerados.EstadoSolicitud;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoAlgoritmoDeConsenso;
import org.springframework.web.multipart.MultipartFile;
import ar.utn.ba.ddsi.models.entities.InformeDeResultados;

/* TODO DTOs Consenso
import ar.utn.ba.ddsi.models.dtos.output.ConsensoOutputDTO;
import ar.utn.ba.ddsi.models.dtos.input.ConsensoInputDTO;
*/

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IAdminService {
  List<ColeccionOutputDTO> getColecciones();
  ColeccionOutputDTO getColeccionByHandle(String handle);
  // ColeccionOutputDTO crearColeccion(ColeccionInputDTO dto);
  ColeccionOutputDTO actualizarColeccion(String id, ColeccionInputDTO dto);
  void eliminarColeccion(String id);

  List<HechoOutputDTO> getHechos(String coleccionId);

  FuenteOutputDTO agregarFuente(String coleccionId, FuenteInputDTO dto);
  boolean eliminarFuenteDeColeccion(String colId, Long fuenteId);
/*
  ConsensoResponseDTO configurarConsenso(Long coleccionId, ConsensoDTO dto);
  Optional<ConsensoResponseDTO> obtenerConsenso(Long coleccionId);
*/
  ColeccionOutputDTO  modificarTipoAlgoritmoConsenso(TipoAlgoritmoDeConsenso tipoAlgoritmo, String id);
  SolicitudOutputDTO aprobarSolicitud(Long id);
  SolicitudOutputDTO denegarSolicitud(Long id);
    void eliminarHecho(Long id);
    SolicitudOutputDTO getSolicitud(Long id);

  InformeDeResultados procesarCsv(MultipartFile file);
    CategoriaOutputDTO crearCategoria(CategoriaInputDTO dto);
    void eliminarCategoria(Long id);
    public CategoriaOutputDTO actualizarCategoria(Long id, CategoriaInputDTO dto);
    public PaginaDTO<SolicitudOutputDTO> obtenerSolicitudesPaginadas(
            int page, int size,
            Long id, String estadoStr, LocalDate fecha
    );
    PaginaDTO<ColeccionOutputDTO> obtenerColeccionesPaginadas(int page, int size, String keyword);
}