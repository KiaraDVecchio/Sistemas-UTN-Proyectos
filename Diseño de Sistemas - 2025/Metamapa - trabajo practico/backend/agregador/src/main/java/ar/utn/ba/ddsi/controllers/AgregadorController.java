package ar.utn.ba.ddsi.controllers;

import ar.utn.ba.ddsi.models.dtos.input.ColeccionInputDTO;
import ar.utn.ba.ddsi.models.dtos.input.FuenteInputDTO;
import ar.utn.ba.ddsi.models.dtos.input.HechoInputDTO;
import ar.utn.ba.ddsi.models.dtos.input.SolicitudInputDTO;
import ar.utn.ba.ddsi.models.dtos.output.*;
import ar.utn.ba.ddsi.models.entities.Categoria;
import ar.utn.ba.ddsi.models.entities.Fuente;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.SolicitudDeEliminacion;
import ar.utn.ba.ddsi.models.entities.enumerados.Origen;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoDeModoNavegacion;
import ar.utn.ba.ddsi.models.repositories.ICategoriaRepository;
import ar.utn.ba.ddsi.models.repositories.IFuenteRepository;
import ar.utn.ba.ddsi.models.repositories.IHechoRepository;
import ar.utn.ba.ddsi.models.repositories.ISolicitudRepository;
import ar.utn.ba.ddsi.services.IAgregadorService;
import ar.utn.ba.ddsi.services.IConsensoService;
import ar.utn.ba.ddsi.services.ISolicitudService;
import ar.utn.ba.ddsi.services.impl.ColeccionService;
import ar.utn.ba.ddsi.services.impl.SolicitudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
public class AgregadorController {

    private final IAgregadorService agregadorService;
    private final ISolicitudService solicitudService;
    private final IFuenteRepository fuenteRepository;
    private final IConsensoService consensoService;
    private final ICategoriaRepository categoriaRepository;
    private final ISolicitudRepository solicitudRepository;
    private final IHechoRepository hechoRepository;
    private final ColeccionService coleccionService;

    public AgregadorController(IAgregadorService agregadorService, ISolicitudService solicitudService, IFuenteRepository fuenteRepository, IConsensoService consensoService, ICategoriaRepository categoriaRepository, ISolicitudRepository solicitudRepository, IHechoRepository hechoRepository, ColeccionService coleccionService){
        this.agregadorService = agregadorService;
        this.solicitudService = solicitudService;
        this.fuenteRepository = fuenteRepository;
        this.consensoService = consensoService;
        this.categoriaRepository = categoriaRepository;
        this.solicitudRepository = solicitudRepository;
        this.hechoRepository = hechoRepository;
        this.coleccionService = coleccionService;
    }

    //pruebas
  /*@GetMapping("/hechos")
  public ResponseEntity<?> getHechos(Set<Fuente> fuentes){
    try{
      return ResponseEntity.ok(this.agregadorService.obtenerTodosLosHechos(fuentes).stream().map(agregadorService::hechoOutputDTO));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error","Solicitud no valida","mensaje", e.getMessage()));
    } catch (NoSuchElementException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("error","Hechos no encontrados","mensaje", e.getMessage()));
    }
    catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Error al buscar los hechos", "mensaje" , e.getMessage()));
    }
  }*/
    @GetMapping("/hechos")
    public ResponseEntity<?> getHechos(){
        try{
            return ResponseEntity.ok(this.agregadorService.obtenerHechos());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error","Solicitud no valida","mensaje", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error","Hechos no encontrados","mensaje", e.getMessage()));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al buscar los hechos", "mensaje" , e.getMessage()));
        }
    }

    @GetMapping("/ultimos-hechos")
    public ResponseEntity<?> getUltimosHechos(){
        try{
            return ResponseEntity.ok(this.agregadorService.getUltimosHechos());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error","Solicitud no valida","mensaje", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error","Hechos no encontrados","mensaje", e.getMessage()));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al buscar los hechos", "mensaje" , e.getMessage()));
        }
    }


    @GetMapping("/hechos/mis")
    public ResponseEntity<List<HechoOutputDTO>> getHechosDelContribuyente(
            @RequestParam("contribuyenteId") Long contribuyenteId) {

        List<HechoOutputDTO> hechos = agregadorService.obtenerHechosPorContribuyente(contribuyenteId);
        return ResponseEntity.ok(hechos);
    }

    @GetMapping("/hechos/mis-filtrado")
    public ResponseEntity<List<HechoOutputDTO>> getHechosDelContribuyenteFiltrado(
            @RequestParam("contribuyenteId") Long contribuyenteId,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String estado
    ) {
        List<HechoOutputDTO> hechos =
                agregadorService.obtenerHechosPorContribuyenteFiltrado(
                        contribuyenteId, titulo, categoria, estado);

        return ResponseEntity.ok(hechos);
    }


    @GetMapping("/paginado")
    public ResponseEntity<Page<HechoOutputDTO>> getHechosPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "fechaAcontecimiento,desc") String sort,

            @RequestParam(required = false) Long idHecho,
            @RequestParam(required = false) String ubicacion,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,

            // NUEVOS PARAMETROS
            @RequestParam(required = false) Double latitud,
            @RequestParam(required = false) Double longitud,
            @RequestParam(required = false) Double radio
    ) {
        // Pasamos los nuevos datos al servicio
        Page<HechoOutputDTO> pagina = agregadorService.obtenerHechosConPaginacion(
                page, size, sort, idHecho, ubicacion, estado, fecha, latitud, longitud, radio
        );
        return ResponseEntity.ok(pagina);
    }

    @PostMapping(value = "/hechos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> crearHecho(
            @RequestPart("hecho") HechoInputDTO hechoInput,
            @RequestPart(value = "multimedia", required = false) MultipartFile[] multimedia) {
        try {
            HechoOutputDTO creado = coleccionService.subirHecho(hechoInput, multimedia);
            // devolver 201 con Location opcional
            return ResponseEntity.created(URI.create("/api/public/hechos/" + creado.getId())).body(creado);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Error creando hecho: " + ex.getMessage());
        }
    }

    // para precargar el form
    @GetMapping("/hechos/{id}")
    public ResponseEntity<Object> getHechoPorId(@PathVariable Long id) {
        return hechoRepository.findById(id)
                .map(h -> ResponseEntity.ok().<Object>body(agregadorService.hechoOutputDTO(h)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .<Object>body(Map.of("error","Hecho no encontrado","mensaje","id="+id)));
    }

    // para guardar cambios
    @PutMapping(value = "/hechos/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> actualizarHecho(
            @PathVariable Long id,
            @RequestPart("hecho") HechoInputDTO hechoInput,
            @RequestPart(value = "multimedia", required = false) MultipartFile[] multimedia,
            @RequestParam(name = "replaceMedia", defaultValue = "false") boolean replaceMedia,
            @RequestParam(value = "deleteExisting", required = false) List<String> deleteExisting) {

        try {
            HechoOutputDTO actualizado = coleccionService.actualizarHecho(id, hechoInput, multimedia, replaceMedia, deleteExisting == null ? List.of() : deleteExisting);
            return ResponseEntity.ok(actualizado);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hecho no encontrado");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error actualizando hecho: " + e.getMessage());
        }
    }


// Busca el método getHechosPorColeccion y reemplázalo con este:

    @GetMapping("/colecciones/{handle}/hechos")
    public ResponseEntity<?> getHechosPorColeccion(
            @PathVariable String handle,
            @RequestParam(value = "modo", defaultValue = "IRRESTRICTA") String modoStr,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String fuente,
            @RequestParam(required = false) String ubicacion,
            @RequestParam(value = "q", required = false) String keyword,

            @RequestParam(value = "fecha_acontecimiento_desde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,

            @RequestParam(value = "fecha_acontecimiento_hasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,

            // --- NUEVOS PARÁMETROS GEOESPACIALES ---
            @RequestParam(required = false) Double latitud,
            @RequestParam(required = false) Double longitud,
            @RequestParam(required = false) Double radio,
            // ----------------------------------------

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            String modoLimpio = modoStr.trim();
            TipoDeModoNavegacion modo = Arrays.stream(TipoDeModoNavegacion.values())
                    .filter(m -> m.name().equalsIgnoreCase(modoLimpio))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Modo inválido: " + modoLimpio));

            return ResponseEntity.ok(
                    agregadorService.obtenerHechosPorColeccion(
                            handle,
                            modo,
                            categoria,
                            fuente,
                            ubicacion,
                            keyword,
                            fechaDesde,
                            fechaHasta,
                            // Pasamos los nuevos datos al servicio
                            latitud,
                            longitud,
                            radio,
                            page,
                            size
                    )
            );

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error","Coleccion no encontrada","mensaje", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error","Error al obtener los hechos","mensaje", e.getMessage()));
        }
    }

    @GetMapping("/colecciones/{handle}/hechos/{hechoId}")
    public ResponseEntity<?> getHechoDetalle(
            @PathVariable String handle,
            @PathVariable Long hechoId) {
        try {
            HechoOutputDTO hecho = agregadorService.obtenerDetalleHecho(handle, hechoId);
            return ResponseEntity.ok(hecho);

        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Recurso no encontrado", "mensaje", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno", "mensaje", e.getMessage()));
        }
    }


    @PostMapping("/solicitudes")
    public ResponseEntity<SolicitudOutputDTO> crearSolicitudDeEliminacion(@RequestBody SolicitudInputDTO dto) {
        try {
            SolicitudDeEliminacion creado = solicitudService.crearSolicitud(dto);
            SolicitudOutputDTO out = SolicitudOutputDTO.fromEntity(creado);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(creado.getId())
                    .toUri();
            return ResponseEntity.created(location).body(out);
        } catch (IllegalArgumentException e) {
            // convierte la excepción a un HTTP 400 con mensaje
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            // HTTP 500 con mensaje genérico
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al crear la solicitud", e);
        }
    }

    @PostMapping("/coleccion/{handle}/vista")
    public void sumarVistaColeccion(@PathVariable String handle){
        try{
            this.agregadorService.sumarVistaColeccion(handle);
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al sumar vista en colección", e);

        }
    }

    @PostMapping("/hecho/{id}/vista")
    public void sumarVistaHecho(@PathVariable Long id){
        try{
            this.agregadorService.sumarVistaHecho(id);
        }catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al sumar vista en colección", e);

        }
    }

    @GetMapping("/hechos-destacados")
    public ResponseEntity<List<HechoOutputDTO>> getTopHechos() {
        List<HechoOutputDTO> top = this.agregadorService.top3Hechos();
        return ResponseEntity.ok(top);
    }

    @GetMapping("/colecciones-destacadas")
    public ResponseEntity<List<ColeccionOutputDTO>> getTopColecciones() {
        List<ColeccionOutputDTO> top = this.agregadorService.top4Colecciones();
        return ResponseEntity.ok(top);
    }

    @PostMapping("/fuentes")
    public ResponseEntity<?> guardarFuente(@RequestBody FuenteInputDTO dto) {
        try {
            if (dto.getUrl() == null || dto.getUrl().isBlank() || dto.getTipo() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Datos inválidos", "mensaje", "url y tipo son obligatorios"));
            }

            Fuente fuente = new Fuente(dto.getUrl(), dto.getTipo());
            Fuente guardada = fuenteRepository.save(fuente);

            return ResponseEntity.status(HttpStatus.CREATED).body(guardada);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al guardar la fuente", "mensaje", e.getMessage()));
        }
    }

    @GetMapping("/fuentes")
    public ResponseEntity<?> obtenerFuentes() {
        try {
            return ResponseEntity.ok(fuenteRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener las fuentes", "mensaje", e.getMessage()));
        }
    }

    @PostMapping("/algoritmo")
    public ResponseEntity<?> aplicarAlgoritmo() {
        try {
            consensoService.aplicarAlgoritmoDeConsenso();
            return ResponseEntity.ok("Algoritmo aplicado");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al aplicar el algoritmo", "mensaje", e.getMessage()));
        }
    }

    //NUEVO
    @GetMapping("/categorias/{categoriaId}")
    public ResponseEntity<?> getCategoria(@PathVariable Long categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Categoria no encontrada"));
        return ResponseEntity.ok(categoria);
    }

    @GetMapping("/categorias")
    public ResponseEntity<?> getCategorias() {
        return ResponseEntity.ok(categoriaRepository.findAll());
    }

    @GetMapping("/solicitudes")
    public ResponseEntity<?> getSolicitudes() {
        return ResponseEntity.ok(this.agregadorService.getSolicitudes());
    }


    @GetMapping("/hechos/fuentes")
    public ResponseEntity<?> getHechosTodasLasFuentes(){
        try{
            Set<Fuente> fuentes = fuenteRepository.findAll().stream().collect(Collectors.toSet());
            return ResponseEntity.ok(this.agregadorService.obtenerTodosLosHechos(fuentes).stream().map(agregadorService::hechoOutputDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error","Solicitud no valida","mensaje", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error","Hechos no encontrados","mensaje", e.getMessage()));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al buscar los hechos", "mensaje" , e.getMessage()));
        }
    }

    @GetMapping("/hechos/id-por-fuente/{idEnFuente}")
    public ResponseEntity<Long> getIdHechoPorIdEnFuente(@PathVariable Long idEnFuente) {
        return hechoRepository.findByIdEnFuenteAndOrigen(idEnFuente, Origen.PROVISTO_POR_CONTRIBUYENTE).map(Hecho::getId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/categorias/paginado")
    public ResponseEntity<PaginaDTO<CategoriaOutputDTO>> obtenerCategoriasPaginadas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PaginaDTO<CategoriaOutputDTO> respuesta = agregadorService.obtenerPaginado(page, size);
        return ResponseEntity.ok(respuesta);
    }


}