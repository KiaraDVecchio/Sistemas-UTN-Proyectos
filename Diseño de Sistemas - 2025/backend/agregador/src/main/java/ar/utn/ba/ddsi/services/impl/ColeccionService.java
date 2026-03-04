package ar.utn.ba.ddsi.services.impl;

import ar.utn.ba.ddsi.Exceptions.ColeccionCreacionException;
import ar.utn.ba.ddsi.models.dtos.input.ColeccionInputDTO;
import ar.utn.ba.ddsi.models.dtos.input.CriterioInputDTO;
import ar.utn.ba.ddsi.models.dtos.input.HechoInputDTO;
import ar.utn.ba.ddsi.models.dtos.output.ColeccionOutputDTO;
import ar.utn.ba.ddsi.models.dtos.output.HechoOutputDTO;
import ar.utn.ba.ddsi.models.entities.Categoria;
import ar.utn.ba.ddsi.models.entities.Coleccion;
import ar.utn.ba.ddsi.models.entities.Fuente;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.Ubicacion;
import ar.utn.ba.ddsi.models.entities.criterios.*;
import ar.utn.ba.ddsi.models.entities.enumerados.Origen;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoDeModoNavegacion;
import ar.utn.ba.ddsi.models.repositories.ICategoriaRepository;
import ar.utn.ba.ddsi.models.repositories.IColeccionRepository;
import ar.utn.ba.ddsi.models.repositories.IFuenteRepository;
import ar.utn.ba.ddsi.models.repositories.IHechoRepository;
import ar.utn.ba.ddsi.modosDeNavegacion.IModoDeNavegacion;
import ar.utn.ba.ddsi.modosDeNavegacion.impl.ModoDeNavegacionFactory;
import ar.utn.ba.ddsi.services.IAgregadorService;
import ar.utn.ba.ddsi.services.IColeccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ColeccionService implements IColeccionService {

    @Autowired
    private ModoDeNavegacionFactory modoDeNavegacionFactory;
    @Autowired
    private IColeccionRepository coleccionRepository;
    @Autowired
    private @Lazy IAgregadorService agregadorService;
    @Autowired
    private IFuenteRepository fuenteRepo;
    @Autowired
    private IHechoRepository hechoRepository;
    @Autowired
    private ICategoriaRepository categoriaRepository;

    private static final Path UPLOAD_DIR = Paths.get("uploads").normalize();

    @Override
    public Coleccion crearColeccion(Coleccion coleccion) {
        // Se puede descomentar si quieres filtrar al crear, ahora es seguro por la optimización del Agregador
        if (!coleccion.getFuentes().isEmpty()){ this.filtrarHechos(coleccion); }
        return coleccionRepository.save(coleccion);
    }

    @Override
    public ColeccionOutputDTO crearColeccion(ColeccionInputDTO dto) {
        try {
            Set<Fuente> fuentes = dto.getFuenteIds().stream()
                    .map(fuenteId -> fuenteRepo.findById(fuenteId)
                            .orElseThrow(() -> new RuntimeException("Fuente no encontrada con id: " + fuenteId)))
                    .collect(Collectors.toSet());

            Coleccion c = new Coleccion(dto.getTitulo(), dto.getDescripcion(), fuentes);
            c.setHandle(dto.getHandle());
            c.setAlgoritmoDeConsenso(dto.getAlgoritmoDeConsenso());

            if (dto.getNuevosCriterios() != null && !dto.getNuevosCriterios().isEmpty()) {
                for (CriterioInputDTO cDto : dto.getNuevosCriterios()) {
                    Criterio criterioEntity = fabricarCriterio(cDto);
                    if (criterioEntity != null) {
                        c.agregarCriterios(criterioEntity);
                    }
                }
            }
            return ColeccionOutputDTO.fromEntity(this.crearColeccion(c));

        } catch (Exception e) {
            e.printStackTrace();
            throw new ColeccionCreacionException("Error al crear la coleccion: " + e.getMessage());
        }
    }

    private Criterio fabricarCriterio(CriterioInputDTO dto) {
        if (dto.getTipoCriterio() == null) return null;
        switch (dto.getTipoCriterio().toUpperCase()) {
            case "TITULO": return new CriterioTitulo(dto.getValorString());
            case "DESCRIPCION": return new CriterioDescripcion(dto.getValorString());
            case "CATEGORIA":
                Categoria cat = categoriaRepository.findByNombreIgnoreCase(dto.getValorString())
                        .orElseThrow(() -> new RuntimeException("La categoría para el criterio no existe: " + dto.getValorString()));
                return new CriterioCategoria(cat);
            case "ORIGEN": return new CriterioOrigen(Origen.valueOf(dto.getValorString()));
            case "FECHA_CARGA":
                if (dto.getFechaDesde() == null || dto.getFechaDesde().isBlank()) return null;
                LocalDate inicioCarga = LocalDate.parse(dto.getFechaDesde());
                LocalDate finCarga = (dto.getFechaHasta() != null && !dto.getFechaHasta().isBlank()) ? LocalDate.parse(dto.getFechaHasta()) : inicioCarga;
                return new CriterioFechaCarga(inicioCarga, finCarga);
            case "FECHA_ACONTECIMIENTO":
                if (dto.getFechaDesde() == null || dto.getFechaDesde().isBlank()) return null;
                LocalDate inicioAcont = LocalDate.parse(dto.getFechaDesde());
                LocalDate finAcont = (dto.getFechaHasta() != null && !dto.getFechaHasta().isBlank()) ? LocalDate.parse(dto.getFechaHasta()) : inicioAcont;
                return new CriterioFechaAcontecimiento(inicioAcont.atStartOfDay(), finAcont.atTime(23, 59, 59));
            case "LUGAR":
                Ubicacion u = null;
                if (dto.getLatitud() != null && dto.getLongitud() != null) {
                    u = new Ubicacion();
                    u.setLatitud(dto.getLatitud());
                    u.setLongitud(dto.getLongitud());
                    u.setProvincia(dto.getProvincia());
                }
                int rango = (dto.getRango() != null) ? dto.getRango() : 0;
                return new CriterioLugar(u, rango, dto.getProvincia());
            default: return null;
        }
    }

    public Coleccion findById(String id) {
        return coleccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Colección no encontrada con id: " + id));
    }

    // --- MÉTODO CORREGIDO ---
    public Coleccion filtrarHechos(Coleccion coleccion) {
        if (coleccion.getFuentes() == null || coleccion.getFuentes().isEmpty()) {
            return coleccion;
        }

        // 1. Obtener hechos actualizados (ya optimizado en AgregadorService)
        List<Hecho> hechosDisponibles = agregadorService.obtenerTodosLosHechos(coleccion.getFuentes())
                .stream()
                .filter(hecho -> !hecho.getFueEliminado())
                .collect(Collectors.toList());

        // 2. Filtrar según criterios de la colección
        List<Hecho> hechosParaLaColeccion;
        if (coleccion.getCriterios().isEmpty()) {
            hechosParaLaColeccion = hechosDisponibles;
        } else {
            hechosParaLaColeccion = hechosDisponibles.stream()
                    .filter(coleccion::cumpleLosCriterios)
                    .collect(Collectors.toList());
        }

        // 3. ACTUALIZACIÓN INTELIGENTE (Diffing)
        // Evita el clear() masivo que destruye la tabla intermedia

        Set<Long> idsNuevos = hechosParaLaColeccion.stream()
                .map(Hecho::getId)
                .collect(Collectors.toSet());

        // A. Eliminar relaciones que ya no corresponden
        // (Hechos que estaban en la colección pero ya no cumplen criterios o no vinieron de la fuente)
        coleccion.getHechos().removeIf(h -> !idsNuevos.contains(h.getId()));

        // B. Agregar solo las relaciones nuevas
        Set<Long> idsActuales = coleccion.getHechos().stream()
                .map(Hecho::getId)
                .collect(Collectors.toSet());

        for (Hecho candidato : hechosParaLaColeccion) {
            if (!idsActuales.contains(candidato.getId())) {
                coleccion.agregarHechos(List.of(candidato));
            }
        }

        return coleccion;
    }

    @Override
    @Transactional
    public void actualizarColecciones() {
        // Podrías optimizar esto trayendo solo las colecciones activas, pero el for está bien si filtrarHechos es eficiente
        List<Coleccion> coleccionesExistentes = new ArrayList<>(coleccionRepository.findAll());
        for (Coleccion coleccion : coleccionesExistentes) {
            filtrarHechos(coleccion);
            coleccionRepository.save(coleccion);
        }
    }

    @Transactional
    public HechoOutputDTO actualizarHecho(Long id, HechoInputDTO input, MultipartFile[] multimedia, boolean replaceMedia, List<String> deleteExisting) {
        Hecho h = hechoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Hecho no encontrado"));

        if (input.getTitulo() == null || input.getTitulo().isBlank()) throw new IllegalArgumentException("Título obligatorio");
        if (input.getDescripcion() == null || input.getDescripcion().isBlank()) throw new IllegalArgumentException("Descripción obligatoria");
        if (input.getCategoria() == null || input.getCategoria().isBlank()) throw new IllegalArgumentException("Categoría obligatoria");
        if (input.getFechaAcontecimiento() == null) throw new IllegalArgumentException("Fecha/hora del acontecimiento obligatoria");

        Categoria categoria = categoriaRepository.findByNombreIgnoreCase(input.getCategoria())
                .orElseGet(() -> {
                    Categoria c = new Categoria();
                    c.setNombre(input.getCategoria());
                    return categoriaRepository.save(c);
                });

        h.setTitulo(input.getTitulo());
        h.setDescripcion(input.getDescripcion());
        h.setCategoria(categoria);
        h.setFechaAcontecimiento(input.getFechaAcontecimiento());
        if (h.getFechaCarga() == null) h.setFechaCarga(LocalDate.now());
        h.setFuenteExterna(input.getFuenteExterna());

        if (h.getUbicacion() == null) h.setUbicacion(new Ubicacion());
        h.getUbicacion().setProvincia(input.getProvincia());
        h.getUbicacion().setLatitud(input.getLatitud());
        h.getUbicacion().setLongitud(input.getLongitud());

        List<String> pathsActuales = h.getPathMultimedia() == null ? new ArrayList<>() : new ArrayList<>(h.getPathMultimedia());
        if (replaceMedia) {
            for (String p : pathsActuales) deletePhysical(p);
            pathsActuales.clear();
        }

        if (!replaceMedia && deleteExisting != null && !deleteExisting.isEmpty()) {
            Set<String> aBorrar = deleteExisting.stream()
                    .filter(Objects::nonNull)
                    .map(this::filenameOf)
                    .collect(Collectors.toSet());
            pathsActuales.removeIf(stored -> aBorrar.contains(filenameOf(stored)));
            for (String s : aBorrar) deletePhysicalByFilename(s);
        }

        if (multimedia != null && multimedia.length > 0) {
            for (MultipartFile file : multimedia) {
                if (file != null && !file.isEmpty()) {
                    try {
                        String rutaWeb = guardarArchivoEnUploads(h.getId(), file);
                        pathsActuales.add(rutaWeb);
                    } catch (IOException e) {
                        throw new RuntimeException("No se pudo guardar archivo: " + file.getOriginalFilename(), e);
                    }
                }
            }
        }
        h.setPathMultimedia(pathsActuales);
        return HechoOutputDTO.fromEntity(hechoRepository.save(h));
    }

    @Override
    public List<Hecho> obtenerHechosPorColeccion(String coleccionId, TipoDeModoNavegacion modoNavegacion) {
        Coleccion coleccion = coleccionRepository.findById(coleccionId)
                .orElseThrow(() -> new RuntimeException("No se encontró la coleccion con id: " + coleccionId));

        List<Hecho> hechos = coleccion.getHechos().stream()
                .filter(h -> !h.getFueEliminado())
                .collect(Collectors.toList());

        IModoDeNavegacion modo = modoDeNavegacionFactory.resolver(modoNavegacion);
        return modo.aplicarModo(hechos, coleccion.getAlgoritmoDeConsenso());
    }

    @Transactional
    public HechoOutputDTO subirHecho(HechoInputDTO input, MultipartFile[] multimedia) {
        if (input.getTitulo() == null || input.getTitulo().isBlank()) throw new IllegalArgumentException("Título obligatorio");
        if (input.getDescripcion() == null || input.getDescripcion().isBlank()) throw new IllegalArgumentException("Descripción obligatoria");
        if (input.getCategoria() == null || input.getCategoria().isBlank()) throw new IllegalArgumentException("Categoría obligatoria");
        if (input.getFechaAcontecimiento() == null) throw new IllegalArgumentException("Fecha/hora del acontecimiento obligatoria");

        Categoria categoria = categoriaRepository.findByNombreIgnoreCase(input.getCategoria())
                .orElseGet(() -> {
                    Categoria c = new Categoria();
                    c.setNombre(input.getCategoria());
                    return categoriaRepository.save(c);
                });

        Hecho hecho = new Hecho();
        hecho.setTitulo(input.getTitulo());
        hecho.setDescripcion(input.getDescripcion());
        hecho.setCategoria(categoria);
        hecho.setFechaAcontecimiento(input.getFechaAcontecimiento());
        hecho.setFechaCarga(LocalDate.now());
        hecho.setFuenteExterna(input.getFuenteExterna());
        hecho.setFueEliminado(false);

        Ubicacion ub = new Ubicacion();
        ub.setProvincia(input.getProvincia());
        ub.setLatitud(input.getLatitud());
        ub.setLongitud(input.getLongitud());
        hecho.setUbicacion(ub);

        Hecho saved = hechoRepository.save(hecho);

        List<String> paths = new ArrayList<>();
        if (multimedia != null && multimedia.length > 0) {
            for (MultipartFile file : multimedia) {
                if (file != null && !file.isEmpty()) {
                    try {
                        String rutaWeb = guardarArchivoEnUploads(saved.getId(), file);
                        paths.add(rutaWeb);
                    } catch (IOException e) {
                        throw new RuntimeException("No se pudo guardar archivo: " + file.getOriginalFilename(), e);
                    }
                }
            }
        }
        saved.setPathMultimedia(paths);
        saved = hechoRepository.save(saved);

        HechoOutputDTO dto = new HechoOutputDTO();
        dto.setId(saved.getId());
        dto.setTitulo(saved.getTitulo());
        dto.setDescripcion(saved.getDescripcion());
        dto.setCategoria(saved.getCategoria() != null ? saved.getCategoria().getNombre() : null);
        dto.setFechaAcontecimiento(saved.getFechaAcontecimiento());
        dto.setFechaCarga(saved.getFechaCarga());
        dto.setIdContenidoMultimedia(saved.getPathMultimedia());
        return dto;
    }

    private static String safeFileName(String original) {
        String base = Paths.get(original).getFileName().toString();
        return base.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String guardarArchivoEnUploads(Long hechoId, MultipartFile file) throws IOException {
        Files.createDirectories(UPLOAD_DIR);
        String nombre = hechoId + "_" + System.currentTimeMillis() + "_" + safeFileName(file.getOriginalFilename());
        Path destino = UPLOAD_DIR.resolve(nombre);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
        }
        return "/uploads/" + nombre;
    }

    private String filenameOf(String pathOrUrl) {
        if (pathOrUrl == null) return null;
        int i = pathOrUrl.lastIndexOf('/');
        return (i >= 0) ? pathOrUrl.substring(i + 1) : pathOrUrl;
    }

    private void deletePhysical(String pathOrUrl) {
        deletePhysicalByFilename(filenameOf(pathOrUrl));
    }

    private void deletePhysicalByFilename(String filename) {
        if (filename == null || filename.isBlank()) return;
        try {
            Path target = UPLOAD_DIR.resolve(filename).normalize();
            if (target.startsWith(UPLOAD_DIR)) {
                Files.deleteIfExists(target);
            }
        } catch (IOException ignore) {
        }
    }

    public Hecho obtenerHechoPorId(Long id) {
        return hechoRepository.findById(id).orElse(null);
    }
}