package ar.utn.ba.ddsi.services;

import ar.utn.ba.ddsi.models.dtos.output.*;
import ar.utn.ba.ddsi.models.entities.Fuente;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoDeModoNavegacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface IAgregadorService {

    PaginaDTO<HechoOutputDTO> obtenerHechosPorColeccion(
            String handle,
            TipoDeModoNavegacion modo,
            String categoria,
            String fuente,
            String ubicacion,
            String keyword,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Double latitud, Double longitud, Double radio,
            int page, int size
    );
    HechoOutputDTO obtenerDetalleHecho(String handle, Long hechoId);
    List<Hecho> obtenerTodosLosHechosDeFuente(Fuente fuente);
    List<Hecho> obtenerTodosLosHechos(Set<Fuente> fuentes);
    HechoOutputDTO hechoOutputDTO(Hecho hecho);
    List<SolicitudOutputDTO> getSolicitudes();
    List<HechoOutputDTO> obtenerHechos();
    void sumarVistaColeccion(String handle);
    public void sumarVistaHecho(Long id);
    List<HechoOutputDTO> top3Hechos();
    List<ColeccionOutputDTO> top4Colecciones();
    List<HechoOutputDTO> obtenerHechosPorContribuyente(Long contribuyenteId);
    List<HechoOutputDTO> obtenerHechosPorContribuyenteFiltrado(
      Long contribuyenteId, String titulo, String categoria, String estado);
    PaginaDTO<CategoriaOutputDTO> obtenerPaginado(int page, int size);
    List<HechoOutputDTO> getUltimosHechos();
    public Page<HechoOutputDTO> obtenerHechosConPaginacion(
            int page, int size, String sort,
            Long id, String ubicacion, String estado, LocalDate fecha,
            Double latitud, Double longitud, Double radio
    );
}
