package ar.utn.ba.ddsi.services.impl;

import ar.utn.ba.ddsi.Exceptions.ColeccionCreacionException;
import ar.utn.ba.ddsi.models.dtos.input.*;
import ar.utn.ba.ddsi.models.dtos.output.*;
import ar.utn.ba.ddsi.models.entities.*;
import ar.utn.ba.ddsi.models.entities.criterios.*;
import ar.utn.ba.ddsi.models.entities.enumerados.EstadoSolicitud;
import ar.utn.ba.ddsi.models.entities.enumerados.Origen;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoAlgoritmoDeConsenso;
import ar.utn.ba.ddsi.models.repositories.*;
import ar.utn.ba.ddsi.services.IAdminService;
import ar.utn.ba.ddsi.services.IColeccionService;
import ar.utn.ba.ddsi.models.entities.InformeDeResultados;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService implements IAdminService {

    private final IColeccionRepository coleccionRepo;
    private final IFuenteRepository fuenteRepo;
    private final ISolicitudRepository solicitudRepo;
    private final IColeccionService coleccionService;
    private final IHechoRepository hechoRepo;
    private final ICategoriaRepository categoriaRepo;
    private static final String DEFAULT_CATEGORY_NAME = "Sin Categoria";

    public AdminService(IColeccionRepository coleccionRepo,
                        IFuenteRepository fuenteRepo,
                        //ConsensoRepository consensoRepo,
                        ISolicitudRepository solicitudRepo,
                        IColeccionService coleccionService,
                        IHechoRepository hechoRepo,
                        ICategoriaRepository categoriaRepo) {
        this.coleccionRepo = coleccionRepo;
        // this.consensoRepo = consensoRepo;
        this.solicitudRepo = solicitudRepo;
        this.coleccionService = coleccionService;
        this.fuenteRepo = fuenteRepo;
        this.hechoRepo = hechoRepo;
        this.categoriaRepo = categoriaRepo;
    }

    private static final DateTimeFormatter FECHA_CSV = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final List<String> HEADER_ESPERADO = List.of(
            "Título","Descripción","Categoría","Latitud","Longitud","Fecha del hecho"
    );

    //API ADMINISTRATIVA

    //OPERACIONES CRUD
    //Devuelve las colecciones
    @Override
    public List<ColeccionOutputDTO> getColecciones() {
        return coleccionRepo.findAll().stream()
                .map(ColeccionOutputDTO::fromEntity) //Convertimos cada entidad a DTO
                .collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public ColeccionOutputDTO getColeccionByHandle(String handle) {
        var coleccion = coleccionRepo.findByHandle(handle)
                .orElseThrow(() -> new NoSuchElementException("Coleccion no encontrada con handle: " + handle));
        return ColeccionOutputDTO.fromEntity(coleccion);
    }


    @Override
    public ColeccionOutputDTO modificarTipoAlgoritmoConsenso(TipoAlgoritmoDeConsenso tipoAlgoritmo, String id) {
        Coleccion coleccion = coleccionRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se puede modificar el Algoritmo de Consenso. Coleccion no encontrada con ID: " + id));
        coleccion.setAlgoritmoDeConsenso(tipoAlgoritmo);
        return ColeccionOutputDTO.fromEntity(coleccionRepo.save(coleccion));
    }

    @Override
    @Transactional
    public ColeccionOutputDTO actualizarColeccion(String id, ColeccionInputDTO dto) {
        Coleccion existing = coleccionRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Coleccion no encontrada: " + id));


        if (dto.getTitulo() != null) existing.setTitulo(dto.getTitulo());
        if (dto.getDescripcion() != null) existing.setDescripcion(dto.getDescripcion());
        if (dto.getAlgoritmoDeConsenso() != null) existing.setAlgoritmoDeConsenso(dto.getAlgoritmoDeConsenso());

        List<CriterioInputDTO> nuevos = dto.getNuevosCriterios() != null ? dto.getNuevosCriterios() : new ArrayList<>();

        existing.getCriterios().clear();

        for (CriterioInputDTO criterioDto : nuevos) {
            Criterio nuevoCriterio = convertirCriterio(criterioDto);
            if (nuevoCriterio != null) {
                existing.getCriterios().add(nuevoCriterio);
            }
        }

        existing = coleccionRepo.save(existing);
        //TODO: definir si actualizamos los hechos apenas editamos la colección (puede tarddar) o si esperamos a la ejecución del scheduler
        //existing = coleccionService.filtrarHechos(existing);

        existing = coleccionRepo.save(existing);

        return ColeccionOutputDTO.fromEntity(existing);
    }
    private Criterio convertirCriterio(CriterioInputDTO dto) {
        if (dto.getTipoCriterio() == null) return null;

        String tipo = dto.getTipoCriterio().toUpperCase();

        try {
            switch (tipo) {
                case "TITULO":
                    return new CriterioTitulo(dto.getValorString());

                case "DESCRIPCION":
                    return new CriterioDescripcion(dto.getValorString());

                case "CATEGORIA":
                    Optional<Categoria> catOpt = categoriaRepo.findByNombreIgnoreCase(dto.getValorString());
                    if (catOpt.isPresent()) {
                        return new CriterioCategoria(catOpt.get());
                    } else {
                        return null;
                    }

                case "ORIGEN":
                    // Convertir String a Enum
                    return new CriterioOrigen(Origen.valueOf(dto.getValorString()));

                case "FECHA_CARGA":

                    LocalDate fcDesde = (dto.getFechaDesde() != null && !dto.getFechaDesde().isBlank())
                            ? LocalDate.parse(dto.getFechaDesde()) : null;
                    LocalDate fcHasta = (dto.getFechaHasta() != null && !dto.getFechaHasta().isBlank())
                            ? LocalDate.parse(dto.getFechaHasta()) : null;
                    return new CriterioFechaCarga(fcDesde, fcHasta);

                case "FECHA_ACONTECIMIENTO":
                    LocalDateTime faDesde = null;
                    LocalDateTime faHasta = null;

                    if (dto.getFechaDesde() != null && !dto.getFechaDesde().isBlank()) {
                        faDesde = LocalDate.parse(dto.getFechaDesde()).atStartOfDay();
                    }
                    if (dto.getFechaHasta() != null && !dto.getFechaHasta().isBlank()) {
                        // Hasta el final del día
                        faHasta = LocalDate.parse(dto.getFechaHasta()).atTime(23, 59, 59);
                    }
                    return new CriterioFechaAcontecimiento(faDesde, faHasta);

                case "LUGAR":
                    if (dto.getLatitud() == null || dto.getLongitud() == null) return null;

                    Ubicacion ubicacion = new Ubicacion();
                    ubicacion.setLatitud(dto.getLatitud());
                    ubicacion.setLongitud(dto.getLongitud());

                    int rango = (dto.getRango() != null) ? dto.getRango() : 0;

                    return new CriterioLugar(ubicacion, rango, dto.getProvincia());

                default:
                    System.out.println("Tipo de criterio no soportado o desconocido: " + tipo);
                    return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    @Transactional
    public void eliminarColeccion(String handle) {
        Coleccion coleccion = coleccionRepo.findByHandle(handle)
                .orElseThrow(() -> new NoSuchElementException(
                        "No se puede eliminar. Coleccion no encontrada con handle: " + handle));

        // forzamos inicialización de la colección si está LAZY (opcional pero seguro)
        coleccion.getHechos().size();

        coleccionRepo.delete(coleccion);
    }

    @Override
    public void eliminarHecho(Long id){
        Hecho hecho = hechoRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "No se puede eliminar. Hecho no encontrado con id: " + id));
        hecho.setFueEliminado(true);
        hechoRepo.save(hecho);
    }


    //Obtención de todos los hechos de una colección
    //Sirve para pruebas sin meterse en modos
    @Override
    public List<HechoOutputDTO> getHechos(String coleccionId) {
        // opcional: validar que la colección exista
        if (!coleccionRepo.existsById(coleccionId)) {
            throw new RuntimeException("Coleccion no encontrada con id: " + coleccionId);
        }

        List<Hecho> hechos = hechoRepo.findByColeccionHandle(coleccionId);

        return hechos.stream()
                .map(this::hechoOutputDTO)   // usás tu mapeador existente
                .collect(Collectors.toList());
    }


    public HechoOutputDTO hechoOutputDTO(Hecho hecho) {
        HechoOutputDTO hechoOutputDTO = new HechoOutputDTO();
        hechoOutputDTO.setId(hecho.getId());
        hechoOutputDTO.setTitulo(hecho.getTitulo());
        hechoOutputDTO.setDescripcion(hecho.getDescripcion());
        hechoOutputDTO.setFechaCarga(hecho.getFechaCarga());
        hechoOutputDTO.setLatitud(hecho.getUbicacion().getLatitud());
        hechoOutputDTO.setLongitud(hecho.getUbicacion().getLongitud());
        hechoOutputDTO.setFechaAcontecimiento(hecho.getFechaAcontecimiento());
        hechoOutputDTO.setCategoria(hecho.getCategoria().getNombre());
        hechoOutputDTO.setFuenteExterna(hecho.getFuenteExterna());
        hechoOutputDTO.setCantVistas(hecho.getCantVistas());
        if(hecho.getEtiquetas() != null){
            hechoOutputDTO.setIdEtiquetas(hecho.getEtiquetas().stream().map(Etiqueta::getId).collect(Collectors.toSet()));
        }
        if(hecho.getPathMultimedia() != null){
            hechoOutputDTO.setIdContenidoMultimedia(new ArrayList<>(hecho.getPathMultimedia()));
        }
        if(hecho.getContribuyente() != null){
            ContribuyenteDTO contrDto = new ContribuyenteDTO();
            contrDto.setId(hecho.getContribuyente().getIdContribuyente());
            contrDto.setNombre(hecho.getContribuyente().getNombre());
            contrDto.setApellido(hecho.getContribuyente().getApellido());
            contrDto.setFechaDeNacimiento(hecho.getContribuyente().getFechaDeNacimiento());
            hechoOutputDTO.setContribuyente(contrDto);
        }
        if(hecho.getFuenteExterna() != null){
            hechoOutputDTO.setFuenteExterna(hecho.getFuenteExterna());
        }

        return hechoOutputDTO;
    }

    //Agregar fuentes de hechos de una colección
    @Override
    public FuenteOutputDTO agregarFuente(String coleccionId, FuenteInputDTO dto) {
        Coleccion coleccion = coleccionRepo.findById(coleccionId)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la coleccion " + coleccionId));

        Fuente fuente = new Fuente(dto.getUrl(), dto.getTipo());
        // Guardar la fuente primero (recomendado) para que tenga id y se mantenga referencialmente consistente
        if (fuenteRepo != null) {
            fuente = fuenteRepo.save(fuente);
        }

        fuenteRepo.save(fuente);
        coleccion.agregarFuentes(fuente);            // Asociamos la fuente desde la colección
        coleccionRepo.save(coleccion);               // Guardamos la colección con la nueva fuente

        return FuenteOutputDTO.fromEntity(fuente);    // Devolvemos la fuente recién agregada
    }

    //Quitar fuentes de hechos de una colección
    @Override
    public boolean eliminarFuenteDeColeccion(String coleccionId, Long fuenteId) {
        Coleccion coleccion = coleccionRepo.findById(coleccionId)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la coleccion " + coleccionId));

        if (coleccion == null) {
            throw new NoSuchElementException("No se encontró la coleccion " + coleccionId); // No existe la colección
        }
        //Intentamos eliminar la fuente con ese id
        boolean removed = coleccion.getFuentes().removeIf(f -> {
            Long id = f.getId();
            return id != null && id.equals(fuenteId);
        });

        if (removed) {
            coleccionRepo.save(coleccion);
            // opcional: fuenteRepo.deleteById(fuenteId);
        }
        return removed;
    }


    //Aprobar una solicitud de eliminación de un hecho.
    @Override
    public SolicitudOutputDTO aprobarSolicitud(Long id) {
        SolicitudDeEliminacion s = solicitudRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la solicitud " + id));
        Long hechoId = s.getHecho().getId();
        Hecho h = hechoRepo.findById(hechoId)
                .orElseThrow(() -> new NoSuchElementException("No se encontró el hecho " + hechoId));
        h.setFueEliminado(true);
        s.setEstado(EstadoSolicitud.ACEPTADA);
        s.setFechaAtencion(LocalDateTime.now());
        hechoRepo.save(h);
        return SolicitudOutputDTO.fromEntity(solicitudRepo.save(s));
    }

    //Denegar una solicitud de eliminación de un hecho.
    @Override
    public SolicitudOutputDTO denegarSolicitud(Long id) {
        SolicitudDeEliminacion s = solicitudRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la solicitud " + id));
        s.setEstado(EstadoSolicitud.RECHAZADA);
        s.setFechaAtencion(LocalDateTime.now());
        return SolicitudOutputDTO.fromEntity(solicitudRepo.save(s));
    }

    @Override
    public SolicitudOutputDTO getSolicitud(Long id){
        SolicitudDeEliminacion s = solicitudRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontró la solicitud " + id));
        return SolicitudOutputDTO.fromEntity(solicitudRepo.save(s));
    }

    @Override
    public InformeDeResultados procesarCsv(MultipartFile file) {
        long tiempo0 = System.currentTimeMillis();

        final String nombreOriginal = (file.getOriginalFilename() != null) ? file.getOriginalFilename() : "archivo.csv";
        final Path destino = guardarArchivo(file);

        final int BATCH_SIZE = 1000; // <<< ajustá si querés 500/1500
        long total = 0;
        long guardados = 0;

        Map<String, Categoria> categoriaCache = new HashMap<>();
        categoriaRepo.findAll().forEach(c -> categoriaCache.put(c.getNombre().trim().toUpperCase(), c));

        List<Hecho> batch = new ArrayList<>(BATCH_SIZE);

        try (BufferedReader br = Files.newBufferedReader(destino, StandardCharsets.UTF_8);
             CSVReader reader = new CSVReaderBuilder(br).build()) {

            String[] header = reader.readNext();

            String[] fila;
            while ((fila = reader.readNext()) != null) {
                total++;
                Hecho h = leerArchivo(fila, categoriaCache);
                batch.add(h);

                if (batch.size() >= BATCH_SIZE) {
                    hechoRepo.saveAll(batch);
                    hechoRepo.flush();
                    batch.clear();
                    guardados += BATCH_SIZE;

                }
            }
        } catch (IOException | CsvValidationException e) {
            throw new IllegalStateException("No se pudo leer el CSV: " + e.getMessage(), e);
        }

        if (!batch.isEmpty()) {
            hechoRepo.saveAll(batch);
            hechoRepo.flush();
            guardados += batch.size();
        }

        long tiempo = System.currentTimeMillis() - tiempo0;

        return InformeDeResultados.builder()
            .nombreOriginal(nombreOriginal)
            .guardadoComo(destino.toString().replace('\\','/'))
            .hechosTotales(total)
            .guardadosTotales(guardados)
            .tiempoTardado(tiempo)
            .build();
    }


    private Path guardarArchivo(MultipartFile file) {
        try {
            Path dir = Paths.get("agregador/src/main/java/ar/utn/ba/ddsi/imports");
            Files.createDirectories(dir);

            String nombre = UUID.randomUUID() + ".csv";
            Path destino = dir.resolve(nombre);

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
            }
            return destino;
        } catch (IOException e) {
            throw new IllegalStateException("No pude guardar el archivo en la carpeta de imports", e);
        }
    }

    private Hecho leerArchivo(String[] fila, Map<String, Categoria> categoriaCache) {
        if (fila == null || fila.length < 6) {
            throw new IllegalArgumentException("Fila inválida: se esperaban 6 columnas");
        }

        String titulo           = safeTrim(fila[0]);
        String descripcion      = safeTrim(fila[1]);
        String categoriaNombre  = safeTrim(fila[2]);
        String latStr           = safeTrim(fila[3]);
        String lonStr           = safeTrim(fila[4]);
        String fechaStr         = safeTrim(fila[5]);

        if (isBlank(titulo) || isBlank(descripcion) || isBlank(categoriaNombre)
                || isBlank(latStr) || isBlank(lonStr) || isBlank(fechaStr)) {
            throw new IllegalArgumentException("Fila con campos requeridos vacíos");
        }

        final double latitud;
        final double longitud;
        final LocalDateTime fechaAcontecimiento;

        try {
            latitud = Double.parseDouble(latStr);
            longitud = Double.parseDouble(lonStr);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Coordenadas inválidas (no numéricas)");
        }

        try {
            LocalDate fecha = LocalDate.parse(fechaStr, FECHA_CSV);
            fechaAcontecimiento = fecha.atStartOfDay();
        } catch (Exception pe) {
            throw new IllegalArgumentException("Fecha inválida (formato esperado dd/MM/yyyy)");
        }

        String key = categoriaNombre.trim().toUpperCase();
        Categoria categoria = categoriaCache.get(key);
        if (categoria == null) {
            categoria = categoriaRepo.save(new Categoria(categoriaNombre));
            categoriaCache.put(key, categoria);
        }

        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setLatitud(latitud);
        ubicacion.setLongitud(longitud);

        return new Hecho(
                titulo,
                descripcion,
                categoria,
                ubicacion,
                fechaAcontecimiento,
                LocalDate.now(),
                Origen.PROVENIENTE_DE_DATASET,
                null
        );
    }

    private static String safeTrim(String s) {
        return (s == null) ? null : s.replace('\u00A0',' ').trim();
    }
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    @Override
    public CategoriaOutputDTO crearCategoria(CategoriaInputDTO dto){
        try{
            Categoria categoria = new Categoria(dto.getNombre());
            categoriaRepo.save(categoria);
            return new CategoriaOutputDTO(dto.getNombre());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void eliminarCategoria(Long id) {

        Categoria categoriaParaEliminar = categoriaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + id));

        if (DEFAULT_CATEGORY_NAME.equalsIgnoreCase(categoriaParaEliminar.getNombre())) {
            throw new RuntimeException("Error: No se puede eliminar la categoría por defecto '" + DEFAULT_CATEGORY_NAME + "'.");
        }

        Optional<Categoria> defaultCategoriaOpt = categoriaRepo.findByNombreIgnoreCase(DEFAULT_CATEGORY_NAME);

        Categoria defaultCategoria;
        if (defaultCategoriaOpt.isPresent()) {
            defaultCategoria = defaultCategoriaOpt.get();
        } else {
            defaultCategoria = new Categoria();
            defaultCategoria.setNombre(DEFAULT_CATEGORY_NAME);
            defaultCategoria = categoriaRepo.save(defaultCategoria);
        }


        List<Hecho> hechosAsociados = hechoRepo.findByCategoria(categoriaParaEliminar);


        if (!hechosAsociados.isEmpty()) {
            for (Hecho hecho : hechosAsociados) {
                hecho.setCategoria(defaultCategoria);
                hechoRepo.save(hecho);
            }
        }

        categoriaRepo.delete(categoriaParaEliminar);
    }

    @Override
    public CategoriaOutputDTO actualizarCategoria(Long id, CategoriaInputDTO dto) {
        Categoria existing = categoriaRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Categoria no encontrada: " + id));
        existing.setNombre(dto.getNombre());
        categoriaRepo.save(existing);
        return new CategoriaOutputDTO(existing.getNombre());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaDTO<SolicitudOutputDTO> obtenerSolicitudesPaginadas(
            int page, int size,
            Long id, String estadoStr, LocalDate fecha
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaEntrada").descending());
        Page<SolicitudDeEliminacion> paginaEntidad;

        // 1. Convertir String a Enum
        EstadoSolicitud estadoEnum = null;
        if (estadoStr != null && !estadoStr.isBlank()) {
            try {
                // Mapeo flexible: "approved" -> ACEPTADA, "rejected" -> RECHAZADA, "pending" -> PENDIENTE
                if("approved".equalsIgnoreCase(estadoStr)) estadoEnum = EstadoSolicitud.ACEPTADA;
                else if("rejected".equalsIgnoreCase(estadoStr)) estadoEnum = EstadoSolicitud.RECHAZADA;
                else if("pending".equalsIgnoreCase(estadoStr)) estadoEnum = EstadoSolicitud.PENDIENTE;
                else estadoEnum = EstadoSolicitud.valueOf(estadoStr.toUpperCase());
            } catch (Exception e) {
                // Si el estado no existe, ignoramos el filtro
            }
        }

        paginaEntidad = solicitudRepo.buscarConFiltros(id, estadoEnum, fecha, pageable);

        List<SolicitudOutputDTO> contenidoDTO = paginaEntidad.getContent().stream()
                .map(SolicitudOutputDTO::fromEntity)
                .collect(Collectors.toList());

        PaginaDTO<SolicitudOutputDTO> respuesta = new PaginaDTO<>();
        respuesta.setContent(contenidoDTO);
        respuesta.setNumber(paginaEntidad.getNumber());
        respuesta.setSize(paginaEntidad.getSize());
        respuesta.setTotalElements(paginaEntidad.getTotalElements());
        respuesta.setTotalPages(paginaEntidad.getTotalPages());
        respuesta.setNumberOfElements(paginaEntidad.getNumberOfElements());
        respuesta.setFirst(paginaEntidad.isFirst());
        respuesta.setLast(paginaEntidad.isLast());

        return respuesta;
    }

    public PaginaDTO<ColeccionOutputDTO> obtenerColeccionesPaginadas(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Coleccion> paginaEntidad;

        if (keyword != null && !keyword.isBlank()) {
            paginaEntidad = coleccionRepo.findByTituloContainingIgnoreCaseOrDescripcionContainingIgnoreCase(keyword, keyword, pageable);
        } else {
            paginaEntidad = coleccionRepo.findAll(pageable);
        }

        List<ColeccionOutputDTO> contenidoDTO = paginaEntidad.getContent().stream()
                .map(ColeccionOutputDTO::fromEntity)
                .collect(Collectors.toList());

        PaginaDTO<ColeccionOutputDTO> respuesta = new PaginaDTO<>();
        respuesta.setContent(contenidoDTO);
        respuesta.setNumber(paginaEntidad.getNumber());
        respuesta.setSize(paginaEntidad.getSize());
        respuesta.setTotalElements(paginaEntidad.getTotalElements());
        respuesta.setTotalPages(paginaEntidad.getTotalPages());
        respuesta.setNumberOfElements(paginaEntidad.getNumberOfElements());
        respuesta.setFirst(paginaEntidad.isFirst());
        respuesta.setLast(paginaEntidad.isLast());

        return respuesta;
    }

}