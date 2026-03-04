package ar.utn.ba.ddsi.controllers;

import ar.utn.ba.ddsi.models.dtos.input.CategoriaInputDTO;
import ar.utn.ba.ddsi.models.dtos.input.ColeccionInputDTO;
import ar.utn.ba.ddsi.models.dtos.input.FuenteInputDTO;
import ar.utn.ba.ddsi.models.dtos.input.SolicitudInputDTO;
import ar.utn.ba.ddsi.models.dtos.output.*;
import ar.utn.ba.ddsi.models.entities.Categoria;
import ar.utn.ba.ddsi.models.entities.enumerados.EstadoSolicitud;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoAlgoritmoDeConsenso;
import ar.utn.ba.ddsi.models.repositories.ICategoriaRepository;
import ar.utn.ba.ddsi.models.repositories.IFuenteRepository;
import ar.utn.ba.ddsi.models.repositories.IHechoRepository;
import ar.utn.ba.ddsi.models.repositories.ISolicitudRepository;
import ar.utn.ba.ddsi.services.IAdminService;
import ar.utn.ba.ddsi.services.ISolicitudService;
import ar.utn.ba.ddsi.models.entities.InformeDeResultados;
import ar.utn.ba.ddsi.services.impl.ColeccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

  private final IAdminService servicio;
  private final ColeccionService coleccionService;
  private final IHechoRepository hechoRepository;
  private final ISolicitudService solicitudService;
  private final IFuenteRepository fuenteRepository;


  public AdminController(IAdminService servicio, ColeccionService coleccionService, ICategoriaRepository categoriaRepository, IHechoRepository hechoRepository, ISolicitudService solicitudService, IFuenteRepository fuenteRepository) {
    this.servicio = servicio;
    this.coleccionService = coleccionService;
    this.hechoRepository = hechoRepository;
    this.solicitudService = solicitudService;
    this.fuenteRepository = fuenteRepository;
  }

  //Obtener la lista completa de colecciones
  @GetMapping("/colecciones")

  public ResponseEntity<?> getColecciones() {
    try{
      return ResponseEntity.ok(this.servicio.getColecciones());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Error al buscar las colecciones","mensaje", e.getMessage() ));
    }
  }

    @GetMapping("/colecciones/{handle}")
    public ResponseEntity<?> getColeccionByHandle(@PathVariable String handle) {
        try {
            ColeccionOutputDTO dto = servicio.getColeccionByHandle(handle);
            return ResponseEntity.ok(dto);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Colección no encontrada", "handle", handle));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al buscar la colección", "mensaje", e.getMessage()));
        }
    }

  //Crear una nueva colección.
  @PostMapping("/colecciones") //Recibe un DTO con los datos de la coleccion
  public ResponseEntity<?> crearColeccion(@RequestBody ColeccionInputDTO dto) {
    try{

      ColeccionOutputDTO coleccionCreada = coleccionService.crearColeccion(dto);
      return ResponseEntity.status(HttpStatus.CREATED).body(coleccionCreada);
    }catch(Exception e){
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error", "Error al crear coleccion", "mensaje", e.getMessage()));
    }

  }

  @PostMapping("/categorias")
  public ResponseEntity<?> crearCategoria(@RequestBody CategoriaInputDTO dto) {
      try{
          CategoriaOutputDTO categoriaCreada = servicio.crearCategoria(dto);
          return ResponseEntity.status(HttpStatus.CREATED).body(categoriaCreada);
      }catch(Exception e){
          return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                  .body(Map.of("error", "Error al crear categoria", "mensaje", e.getMessage()));
      }
  }

    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<?> eliminarCategoria(@PathVariable Long id) {
        try {
            servicio.eliminarCategoria(id);
            return ResponseEntity.ok(Map.of("mensaje", "Categoria borrada correctamente"));

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Error de integridad de datos", "mensaje", "No se puede eliminar la categoría porque está siendo usada por uno o más hechos."));

        } catch (Exception e) {
            return ResponseEntity.status((HttpStatus.NOT_FOUND))
                    .body(Map.of("error", "Error al eliminar la categoria", "mensaje", e.getMessage()));
        }
    }

    @PutMapping("/categorias/{id}")
    public ResponseEntity<?> actualizarCategoria(@PathVariable Long id, @RequestBody CategoriaInputDTO dto) {
        try{
            return ResponseEntity.ok(servicio.actualizarCategoria(id, dto));
        } catch (NoSuchElementException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Categoria no encontrada", "mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Error al editar la categoria", "mensaje", e.getMessage()));

        }
    }



    //Actualiza coleccion por id
  @PutMapping("/colecciones/{id}")
  public ResponseEntity<?> actualizarColeccion(@PathVariable String id, @RequestBody ColeccionInputDTO dto) {
    try{
      return ResponseEntity.ok(servicio.actualizarColeccion(id, dto));
    } catch (NoSuchElementException e){
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("error", "Coleccion no encontrada", "mensaje", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error", "Error al editar la coleccion", "mensaje", e.getMessage()));

    }
  }


  //Borra coleccion por su id
  @DeleteMapping("/colecciones/{handle}")
  public ResponseEntity<?> eliminarColeccion(@PathVariable String handle) {
    try{
      servicio.eliminarColeccion(handle);
      return ResponseEntity.ok(Map.of("mensaje", "Coleccion borrada correctamente"));
    } catch(Exception e) {
      return ResponseEntity.status((HttpStatus.NOT_FOUND))
          .body(Map.of("error", "Error al eliminar la coleccion", "mensaje", e.getMessage()));
    }
  }

  @PostMapping("hechos/{id}/eliminar")
  public ResponseEntity<?> eliminarHecho(@PathVariable Long id) {
      try{
          servicio.eliminarHecho(id);
          return ResponseEntity.ok(Map.of("mensaje", "Hecho eliminado correctamente"));
      } catch (Exception e) {
          return ResponseEntity.status((HttpStatus.NOT_FOUND))
                  .body(Map.of("error", "Error al eliminar el hecho", "mensaje", e.getMessage()));
      }
  }

  //Obtener todos los hechos de una coleccion especifica
  @GetMapping("/colecciones/{colId}/hechos")
  public ResponseEntity<?> getHechos(@PathVariable String colId) {
    try {
      return ResponseEntity.ok(this.servicio.getHechos(colId));
    } catch (NoSuchElementException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("error", "Coleccion no encontrada", "mensaje", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Error al buscar los hechos", "mensaje", e.getMessage()));
    }
  }

  //Agregar una fuente de hechos a una coleccion
  @PostMapping("/colecciones/{colId}/fuentes") //Recibe el DTO de la fuente

  public ResponseEntity<?> agregarFuente(@PathVariable String colId, @RequestBody FuenteInputDTO dto) {
    try {
      return ResponseEntity.status(HttpStatus.CREATED).body(servicio.agregarFuente(colId, dto));
    } catch (NoSuchElementException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("error", "Coleccion no encontrada", "mensaje", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error","Error al agregar la fuente a la coleccion", "mensaje", e.getMessage()));
    }
  }


  //Borrar una fuente de una coleccion especifica
  @DeleteMapping("/colecciones/{colId}/fuentes/{fuenteId}")
  public ResponseEntity<?> eliminarFuente(@PathVariable String colId, @PathVariable Long fuenteId) {
    try {
      boolean removed = servicio.eliminarFuenteDeColeccion(colId, fuenteId);
      if (!removed) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", "La fuente no pertenece a la coleccion", "mensaje", "ID: " + fuenteId));
      }
      return ResponseEntity.ok(Map.of("mensaje", "Fuente borrada correctamente"));
    } catch (NoSuchElementException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("error", "Coleccion no encontrada", "mensaje", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Error al eliminar la fuente de la coleccion", "mensaje", e.getMessage()));
    }

  }
  //Aprobar una solicitud de eliminación por id
  @PostMapping("/solicitudes-eliminacion/{id}/aprobar")
  public ResponseEntity<?> aprobarSolicitud(@PathVariable Long id) {
    try {
      return ResponseEntity.ok(this.servicio.aprobarSolicitud(id));
    } catch (NoSuchElementException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("error", "Solicitud no encontrada", "mensaje", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Error al buscar la solicitud", "mensaje", e.getMessage()));
    }
  }

  //Rechazar solicitud de eliminacion por id
  @PostMapping("/solicitudes-eliminacion/{id}/denegar")
  public ResponseEntity<?> denegarSolicitud(@PathVariable Long id){

    try {
      return ResponseEntity.ok(this.servicio.denegarSolicitud(id));
    }  catch (NoSuchElementException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("error", "Solicitud no encontrada", "mensaje", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Error al buscar la solicitud", "mensaje", e.getMessage()));
    }

  }

  @PutMapping("/colecciones/{colId}/consenso")
  public ResponseEntity<?> modificarTipoAlgoritmoConsenso(@PathVariable("colId") String colId, @RequestBody TipoAlgoritmoDeConsenso tipoAlgoritmo){
    try {
      return ResponseEntity.ok(this.servicio.modificarTipoAlgoritmoConsenso(tipoAlgoritmo, colId));
    } catch (NoSuchElementException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("error", "Coleccion no encontrada", "mensaje", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Error al buscar la coleccion", "mensaje", e.getMessage()));
    }

  }

  @GetMapping("/resumen")
  public ResponseEntity<ResumenDTO> getResumen(){
    //try {
      ResumenDTO dto = new ResumenDTO();
      dto.totalFuentes = fuenteRepository.count();
      dto.solicitudesPendientes = solicitudService.contarPorEstado(EstadoSolicitud.PENDIENTE);
      dto.totalHechos = hechoRepository.count();
      return ResponseEntity.ok(dto);
   // }catch (Exception e){
   //   return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
   //       .body(Map.of("error","Error al obtener el resumen","mensaje", e.getMessage()));
   // }
  }


  @PostMapping("/actualizar")
  public ResponseEntity<?> actualizarColecciones() {
    coleccionService.actualizarColecciones();
    return ResponseEntity.ok("ok");
  }

  @GetMapping("/solicitudes/{id}")
    public ResponseEntity<?> solicitudes(@PathVariable Long id){
      try {
          return ResponseEntity.ok(this.servicio.getSolicitud(id));
      } catch (NoSuchElementException e) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND)
                  .body(Map.of("error", "Solicitud no encontrada", "mensaje", e.getMessage()));
      } catch (Exception e) {
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                  .body(Map.of("error", "Error al buscar la solicitud", "mensaje", e.getMessage()));
      }
  }

  @PostMapping("/import/hechos/csv")
  public ResponseEntity<?> importarHechosCsv(
      @RequestParam("archivo") MultipartFile archivo
  ) {
    try {
      if (archivo == null || archivo.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of(
            "error", "Archivo faltante o vacío",
            "detalle", "Enviá el campo 'archivo' como multipart/form-data"
        ));
      }

      InformeDeResultados resultado = servicio.procesarCsv(archivo);
      return ResponseEntity.ok(resultado);

    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
          "error", "CSV inválido",
          "mensaje", e.getMessage()
      ));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
          "error", "Error al procesar el CSV",
          "mensaje", e.getMessage()
      ));
    }
  }


    @GetMapping("/solicitudes/paginado")
    public ResponseEntity<PaginaDTO<SolicitudOutputDTO>> obtenerSolicitudes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,

            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        PaginaDTO<SolicitudOutputDTO> resultado = servicio.obtenerSolicitudesPaginadas(page, size, id, estado, fecha);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/colecciones/paginado")
    public ResponseEntity<PaginaDTO<ColeccionOutputDTO>> getColeccionesPaginadas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword
    ) {

        PaginaDTO<ColeccionOutputDTO> respuesta = servicio.obtenerColeccionesPaginadas(page, size, keyword);
        return ResponseEntity.ok(respuesta);
    }
}


