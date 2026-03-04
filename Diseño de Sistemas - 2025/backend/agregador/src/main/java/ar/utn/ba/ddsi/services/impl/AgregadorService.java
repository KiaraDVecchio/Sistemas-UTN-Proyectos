package ar.utn.ba.ddsi.services.impl;

import ar.utn.ba.ddsi.adapters.AdapterFuenteDinamica;
import ar.utn.ba.ddsi.adapters.AdapterFuenteEstatica;
import ar.utn.ba.ddsi.adapters.AdapterFuenteProxy;
import ar.utn.ba.ddsi.models.dtos.input.FiltroHechosInput;
import ar.utn.ba.ddsi.models.dtos.output.*;
import ar.utn.ba.ddsi.models.entities.*;
import ar.utn.ba.ddsi.models.entities.enumerados.Origen;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoAlgoritmoDeConsenso;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoDeModoNavegacion;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoFuente;
import ar.utn.ba.ddsi.models.repositories.*;
import ar.utn.ba.ddsi.services.IAgregadorService;
import ar.utn.ba.ddsi.services.IColeccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AgregadorService implements IAgregadorService {

    @Autowired
    IColeccionService coleccionService;

    private final AdapterFuenteDinamica adapterFuenteDinamica;
    private final AdapterFuenteEstatica adapterFuenteEstatica;
    private final AdapterFuenteProxy adapterFuenteProxy;
    private final NormalizadorService normalizadorService;
    private final IHechoRepository hechoRepository;
    private final ICategoriaRepository categoriaRepository;
    private final ISolicitudRepository solicitudesRepo;
    private final IColeccionRepository coleccionRepo;

    @Autowired
    private IFuenteRepository fuenteRepository;

    public AgregadorService(AdapterFuenteDinamica adapterFuenteDinamica, AdapterFuenteEstatica adapterFuenteEstatica, AdapterFuenteProxy adapterFuenteProxy, NormalizadorService normalizadorService, IHechoRepository hechoRepository, ICategoriaRepository categoriaRepository, ISolicitudRepository solicitudesRepo, IColeccionRepository coleccionRepo) {
        this.adapterFuenteDinamica = adapterFuenteDinamica;
        this.adapterFuenteEstatica = adapterFuenteEstatica;
        this.adapterFuenteProxy = adapterFuenteProxy;
        this.normalizadorService = normalizadorService;
        this.hechoRepository = hechoRepository;
        this.categoriaRepository = categoriaRepository;
        this.solicitudesRepo = solicitudesRepo;
        this.coleccionRepo = coleccionRepo;
    }

    @Value("${minio.public-url}")
    private String minioPublicUrl;
    @Value("${minio.bucket}")
    private String bucketName;

    @Override
    public List<SolicitudOutputDTO> getSolicitudes() {
        return solicitudesRepo.findAll().stream()
                .map(SolicitudOutputDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<Hecho> obtenerTodosLosHechos(Set<Fuente> fuentes) {
        if (fuentes == null || fuentes.isEmpty()) {
            throw new IllegalArgumentException("No se especificaron fuentes.");
        }

        // 1. Obtener los hechos crudos de las fuentes
        List<Hecho> hechosNuevos = fuentes.parallelStream()
                .flatMap(f -> {
                    try {
                        return obtenerTodosLosHechosDeFuente(f).stream();
                    } catch (Exception e) {
                        //System.err.println("⚠️ ALERTA: Falló la fuente " + f.getUrl() + " (" + f.getTipo() + "). Causa: " + e.getMessage());
                        return java.util.stream.Stream.empty();
                    }
                })
                .map(h -> {
                    try {
                        // OJO: Si normalizar llama a la RAE (API externa), esto será lento.
                        normalizadorService.normalizar(h);
                        return h;
                    } catch (IllegalArgumentException e) {
                        //System.err.println("Error normalizando hecho: " + h.getTitulo());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (hechosNuevos.isEmpty()) {
            //System.err.println("No se pudieron obtener hechos nuevos (o todas las fuentes fallaron).");
            return new ArrayList<>();
        }


        List<Hecho> hechosParaGuardar = new ArrayList<>();

        Map<Origen, List<Hecho>> hechosPorOrigen = hechosNuevos.stream()
                .filter(h -> h.getOrigen() != null)
                .collect(Collectors.groupingBy(Hecho::getOrigen));

        Map<Long, Hecho> cacheHechosExistentes = new HashMap<>();

        for (Map.Entry<Origen, List<Hecho>> entry : hechosPorOrigen.entrySet()) {
            Origen origen = entry.getKey();
            List<Long> ids = entry.getValue().stream()
                    .map(Hecho::getIdEnFuente)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!ids.isEmpty()) {
                List<Hecho> encontrados = hechoRepository.findByIdEnFuenteInAndOrigen(ids, origen);

                for (Hecho h : encontrados) {
                    cacheHechosExistentes.put(h.getIdEnFuente(), h);
                }
            }
        }

        for (Hecho hechoNuevo : hechosNuevos) {
            Hecho existente = null;

            if (hechoNuevo.getIdEnFuente() != null && hechoNuevo.getOrigen() != null) {
                existente = cacheHechosExistentes.get(hechoNuevo.getIdEnFuente());
            }

            if (existente == null && hechoNuevo.getIdEnFuente() == null) {
                List<Hecho> porTitulo = hechoRepository.findByTitulo(hechoNuevo.getTitulo());
                if (!porTitulo.isEmpty()) {
                    existente = porTitulo.get(0);
                }
            }

            if (existente != null) {
                if (existente.huboEdicion(hechoNuevo)) {
                    existente.actualizarDesde(hechoNuevo);
                    hechosParaGuardar.add(existente);
                }
            } else {
                hechosParaGuardar.add(hechoNuevo);
            }
        }


        if (!hechosParaGuardar.isEmpty()) {
            int batchSize = 1000;
            for (int i = 0; i < hechosParaGuardar.size(); i += batchSize) {
                int fin = Math.min(i + batchSize, hechosParaGuardar.size());
                List<Hecho> subLista = hechosParaGuardar.subList(i, fin);

                hechoRepository.saveAll(subLista);
                hechoRepository.flush();
            }
        }

        return hechosParaGuardar;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HechoOutputDTO> obtenerHechos() {
        return this.hechoRepository.findAll().stream()
                .map(this::hechoOutputDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HechoOutputDTO> getUltimosHechos() {
        List<Hecho> hechos = hechoRepository.findTop1000ByFueEliminadoFalseOrderByFechaCargaDesc();
        return hechos.stream()
                .map(this::hechoOutputDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<HechoOutputDTO> obtenerHechosPorContribuyente(Long contribuyenteId) {
        Fuente fuenteDinamica = fuenteRepository.findByTipo(TipoFuente.DINAMICA).stream().findFirst().orElse(null);

        if (fuenteDinamica != null) {
            try {
                List<Hecho> hechosFrescos = adapterFuenteDinamica.obtenerHechos(fuenteDinamica.getUrl());
                List<Hecho> hechosUsuario = hechosFrescos.stream()
                        .filter(h -> h.getContribuyente() != null && h.getContribuyente().getIdContribuyente().equals(contribuyenteId))
                        .collect(Collectors.toList());

                for (Hecho fresco : hechosUsuario) {
                    Hecho local = hechoRepository.findByIdEnFuenteAndOrigen(fresco.getIdEnFuente(), fresco.getOrigen())
                            .orElse(null);

                    if (local != null) {
                        local.setTieneEdicionPendiente(fresco.getTieneEdicionPendiente());
                        local.actualizarDesde(fresco);
                        hechoRepository.save(local);
                    } else {
                        hechoRepository.save(fresco);
                    }
                }
            } catch (Exception e) {
                System.err.println("⚠️ No se pudo sincronizar hechos del usuario en tiempo real: " + e.getMessage());
            }
        }

        List<Hecho> hechos = hechoRepository.buscarPorIdContribuyente(contribuyenteId);
        return hechos.stream()
                .map(this::hechoOutputDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<HechoOutputDTO> obtenerHechosPorContribuyenteFiltrado(
            Long contribuyenteId, String titulo, String categoria, String estado
    ) {
        List<Hecho> hechos = hechoRepository.buscarPorIdContribuyente(contribuyenteId);
        List<HechoOutputDTO> dtos = hechos.stream()
                .map(this::hechoOutputDTO)
                .collect(Collectors.toList());

        return dtos.stream()
                .filter(dto -> {
                    if (titulo != null && !titulo.isBlank()) {
                        if (dto.getTitulo() == null || !dto.getTitulo().toLowerCase().contains(titulo.toLowerCase())) {
                            return false;
                        }
                    }
                    if (categoria != null && !categoria.isBlank()) {
                        if (dto.getCategoria() == null || !dto.getCategoria().equalsIgnoreCase(categoria)) {
                            return false;
                        }
                    }
                    if (estado != null && !estado.isBlank()) {
                        boolean esEditable = dto.isEditable();
                        if ("editable".equalsIgnoreCase(estado) && !esEditable) return false;
                        if ("expirado".equalsIgnoreCase(estado) && esEditable) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HechoOutputDTO> obtenerHechosConPaginacion(
            int page, int size, String sort,
            Long id, String ubicacion, String estado, LocalDate fecha,
            Double latitud, Double longitud, Double radio
    ) {
        Sort sortObj;
        String[] parts = sort.split(",", 2);
        String campo = parts[0];
        String direccion = (parts.length == 2) ? parts[1] : "asc";

        switch (campo) {
            case "fechaAcontecimiento": campo = "fecha_acontecimiento"; break;
            case "fechaCarga": campo = "fecha_carga"; break;
            case "titulo": campo = "titulo"; break;
            case "id": campo = "id"; break;
            default: campo = "fecha_carga"; break;
        }

        if ("desc".equalsIgnoreCase(direccion)) {
            sortObj = Sort.by(campo).descending();
        } else {
            sortObj = Sort.by(campo).ascending();
        }

        Pageable pageable = PageRequest.of(page, Math.max(1, Math.min(size, 200)), sortObj);
        Boolean eliminado = null;
        if ("approved".equalsIgnoreCase(estado)) eliminado = false;
        else if ("rejected".equalsIgnoreCase(estado)) eliminado = true;

        Page<Hecho> paginaEntidad = hechoRepository.buscarConFiltros(
                id, ubicacion, eliminado, fecha, latitud, longitud, radio, pageable
        );

        return paginaEntidad.map(this::hechoOutputDTO);
    }

    @Override
    public void sumarVistaColeccion(String handle) {
        Coleccion c = coleccionRepo.findByHandle(handle)
                .orElseThrow(() -> new NoSuchElementException("Coleccion no encontrada: " + handle));
        Integer actuales = c.getCantVistas();
        if (actuales == null) actuales = 0;
        c.setCantVistas(actuales + 1);
        coleccionRepo.save(c);
    }

    @Override
    public void sumarVistaHecho(Long id) {
        Hecho h = hechoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Hecho no encontrado: " + id));
        Integer actuales = h.getCantVistas();
        if (actuales == null) actuales = 0;
        h.setCantVistas(actuales + 1);
        hechoRepository.save(h);
    }

    public List<HechoOutputDTO> top3Hechos() {
        List<Hecho> hechos = this.hechoRepository.findTop3ByFueEliminadoFalseOrderByCantVistasDesc();
        return hechos.stream().map(this::hechoOutputDTO).collect(Collectors.toList());
    }

    public List<ColeccionOutputDTO> top4Colecciones() {
        List<Coleccion> cols = this.coleccionRepo.findTop4ByOrderByCantVistasDesc();
        return cols.stream().map(ColeccionOutputDTO::fromEntity).collect(Collectors.toList());
    }

    private Categoria ensureCategoria(String nombre) {
        return categoriaRepository.findByNombreIgnoreCase(nombre)
                .orElseGet(() -> categoriaRepository.save(new Categoria(nombre)));
    }

    @Override
    public List<Hecho> obtenerTodosLosHechosDeFuente(Fuente fuente) {
        List<Hecho> hechos = new ArrayList<>();
        switch (fuente.getTipo()) {
            case ESTATICA:
                hechos.addAll(adapterFuenteEstatica.obtenerHechos(fuente.getUrl()));
                break;
            case PROXY:
                hechos.addAll(adapterFuenteProxy.obtenerHechos(fuente.getUrl()));
                break;
            case DINAMICA:
                hechos.addAll(adapterFuenteDinamica.obtenerHechos(fuente.getUrl()));
                break;
        }
        return hechos;
    }

    @Override
    public HechoOutputDTO hechoOutputDTO(Hecho hecho) {
        HechoOutputDTO hechoOutputDTO = new HechoOutputDTO();

        hechoOutputDTO.setCantVistas(hecho.getCantVistas());
        hechoOutputDTO.setId(hecho.getId());
        hechoOutputDTO.setTitulo(hecho.getTitulo());
        hechoOutputDTO.setDescripcion(hecho.getDescripcion());
        if (hecho.getCategoria() != null) {
            hechoOutputDTO.setCategoria(hecho.getCategoria().getNombre());
        }
        hechoOutputDTO.setFechaCarga(hecho.getFechaCarga());
        hechoOutputDTO.setFechaAcontecimiento(hecho.getFechaAcontecimiento());
        hechoOutputDTO.setFueEliminado(hecho.getFueEliminado());
        hechoOutputDTO.setIdEnFuente(hecho.getIdEnFuente());

        if (hecho.getUbicacion() != null) {
            hechoOutputDTO.setLatitud(hecho.getUbicacion().getLatitud());
            hechoOutputDTO.setLongitud(hecho.getUbicacion().getLongitud());
            hechoOutputDTO.setProvincia(hecho.getUbicacion().getProvincia());
        }

        if (hecho.getEtiquetas() != null) {
            hechoOutputDTO.setIdEtiquetas(hecho.getEtiquetas().stream().map(Etiqueta::getId).collect(Collectors.toSet()));
        }
        hechoOutputDTO.setTieneEdicionPendiente(Boolean.TRUE.equals(hecho.getTieneEdicionPendiente()));
        hechoOutputDTO.setIdContenidoMultimedia(
                hecho.getPathMultimedia() != null ? hecho.getPathMultimedia() : List.of()
        );
        if (hechoOutputDTO.getIdContenidoMultimedia() != null) {
            hechoOutputDTO.setIdContenidoMultimedia(
                hechoOutputDTO.getIdContenidoMultimedia().stream()
                    .filter(Objects::nonNull)
                    .map(name -> {
                        if (name.startsWith("http")) return name;
                        String base = minioPublicUrl.endsWith("/")
                            ? minioPublicUrl
                            : minioPublicUrl + "/";
                        return base + bucketName + "/" + name;
                    })
                    .toList()
            );
        }

        if (hecho.getContribuyente() != null) {
            ContribuyenteDTO contrDto = new ContribuyenteDTO();
            contrDto.setId(hecho.getContribuyente().getIdContribuyente());
            contrDto.setNombre(hecho.getContribuyente().getNombre());
            contrDto.setApellido(hecho.getContribuyente().getApellido());
            contrDto.setFechaDeNacimiento(hecho.getContribuyente().getFechaDeNacimiento());
            hechoOutputDTO.setContribuyente(contrDto);
        }
        if (hecho.getFuenteExterna() != null) {
            hechoOutputDTO.setFuenteExterna(hecho.getFuenteExterna());
        }

        LocalDate fc = hecho.getFechaCarga();
        if (fc != null) {
            long dias = ChronoUnit.DAYS.between(fc, LocalDate.now());
            boolean editable = dias < 7;
            hechoOutputDTO.setEditable(editable);
            hechoOutputDTO.setDiasRestantes(editable ? (int) (7 - dias) : 0);
        } else {
            hechoOutputDTO.setEditable(false);
            hechoOutputDTO.setDiasRestantes(0);
        }

        return hechoOutputDTO;
    }



    @Override
    public PaginaDTO<HechoOutputDTO> obtenerHechosPorColeccion(
            String handle, TipoDeModoNavegacion modo, String categoria, String fuente, String ubicacion,
            String keyword, LocalDate fechaDesde, LocalDate fechaHasta,
            Double latitud, Double longitud, Double radio, int page, int size
    ) {
        // 1. Obtener información de la colección para saber qué algoritmo de consenso aplica
        // Esto es rápido porque busca por ID (handle)
        Coleccion coleccionInfo = coleccionService.findById(handle);
        TipoAlgoritmoDeConsenso algoritmoActual = coleccionInfo.getAlgoritmoDeConsenso();

        // 2. Convertir el String 'fuente' al Enum 'Origen' para la consulta JPA
        Origen origenEnum = null;
        if (fuente != null && !fuente.isBlank()) {
            try {
                origenEnum = Origen.valueOf(fuente);
            } catch (IllegalArgumentException e) {
                // Si el string no coincide con ningún ENUM, ignoramos el filtro o lo dejamos null
                // System.err.println("Valor de fuente inválido: " + fuente);
            }
        }

        // 3. Determinar si filtramos por "Modo Curado"
        boolean modoCurado = TipoDeModoNavegacion.CURADO.equals(modo);

        // 4. Preparar la paginación
        Pageable pageable = PageRequest.of(page, size);

        // 5. LLAMADA OPTIMIZADA AL REPOSITORIO
        // Esta línea hace la magia: DB filtra y devuelve solo 'size' elementos (ej. 12).
        Page<Hecho> paginaHechos = hechoRepository.findHechosByColeccionHandlePaginado(
                handle,
                (categoria != null && !categoria.isBlank()) ? categoria : null,
                origenEnum,
                (keyword != null && !keyword.isBlank()) ? keyword : null,
                fechaDesde,
                fechaHasta,
                (ubicacion != null && !ubicacion.isBlank()) ? ubicacion : null,
                modoCurado,
                pageable
        );

        // 6. Mapear los resultados (Entidad -> DTO)
        // Solo iteramos sobre los 10-12 elementos devueltos por la DB, no sobre 10.000
        List<HechoOutputDTO> contenido = paginaHechos.getContent().stream()
                .map(hecho -> mapearConConsenso(hecho, algoritmoActual))
                .collect(Collectors.toList());

        // NOTA SOBRE GEOLOCALIZACIÓN (Lat/Lon/Radio):
        // La consulta JPQL actual filtra por "ubicacion" (String/Provincia).
        // Si necesitas filtrar estrictamente por radio matemático (Haversine) sobre los 10k registros,
        // deberías implementar una Native Query espacial.
        // Por ahora, para arreglar el rendimiento crítico, nos basamos en los filtros de la DB.

        // 7. Construir el objeto de respuesta paginado
        PaginaDTO<HechoOutputDTO> paginaDTO = new PaginaDTO<>();
        paginaDTO.setContent(contenido);
        paginaDTO.setNumber(paginaHechos.getNumber());
        paginaDTO.setSize(paginaHechos.getSize());
        paginaDTO.setTotalElements(paginaHechos.getTotalElements()); // Total real en DB
        paginaDTO.setTotalPages(paginaHechos.getTotalPages());
        paginaDTO.setNumberOfElements(paginaHechos.getNumberOfElements());
        paginaDTO.setFirst(paginaHechos.isFirst());
        paginaDTO.setLast(paginaHechos.isLast());

        return paginaDTO;
    }

    private boolean cumpleFiltros(Hecho hecho, String categoria, String fuente, String ubicacion,
                                  String keyword, LocalDate fechaDesde, LocalDate fechaHasta,
                                  Double latitud, Double longitud, Double radio) {
        if (latitud != null && longitud != null && radio != null) {
            if (hecho.getUbicacion() == null || hecho.getUbicacion().getLatitud() == null || hecho.getUbicacion().getLongitud() == null) {
                return false;
            }
            double dist = calcularDistanciaKm(latitud, longitud, hecho.getUbicacion().getLatitud(), hecho.getUbicacion().getLongitud());
            if (dist > radio) return false;
        }

        if (categoria != null && !categoria.isBlank()) {
            if (hecho.getCategoria() == null || hecho.getCategoria().getNombre() == null || !hecho.getCategoria().getNombre().equalsIgnoreCase(categoria)) {
                return false;
            }
        }

        if (fuente != null && !fuente.isBlank()) {
            if (hecho.getOrigen() == null || !hecho.getOrigen().name().equalsIgnoreCase(fuente)) {
                return false;
            }
        }

        if (ubicacion != null && !ubicacion.isBlank()) {
            if (hecho.getUbicacion() == null || hecho.getUbicacion().getProvincia() == null) return false;
            if (!hecho.getUbicacion().getProvincia().toLowerCase().contains(ubicacion.toLowerCase())) return false;
        }

        if (keyword != null && !keyword.isBlank()) {
            String k = normalizar(keyword);
            String tituloNorm = normalizar(hecho.getTitulo());
            String descNorm = normalizar(hecho.getDescripcion());
            if (!tituloNorm.contains(k) && !descNorm.contains(k)) return false;
        }

        if (fechaDesde != null) {
            if (hecho.getFechaAcontecimiento() == null || hecho.getFechaAcontecimiento().toLocalDate().isBefore(fechaDesde)) return false;
        }

        if (fechaHasta != null) {
            if (hecho.getFechaAcontecimiento() == null || hecho.getFechaAcontecimiento().toLocalDate().isAfter(fechaHasta)) return false;
        }

        return true;
    }

    private double calcularDistanciaKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private HechoOutputDTO mapearConConsenso(Hecho hecho, TipoAlgoritmoDeConsenso algoritmo) {
        HechoOutputDTO dto = this.hechoOutputDTO(hecho);
        if (algoritmo == null) {
            dto.setConsensuado(true);
        } else {
            Boolean estaAprobado = hecho.getConsensoPorAlgoritmo().get(algoritmo);
            dto.setConsensuado(Boolean.TRUE.equals(estaAprobado));
        }
        return dto;
    }

    @Override
    public HechoOutputDTO obtenerDetalleHecho(String handle, Long hechoId) {
        Coleccion coleccionInfo = coleccionService.findById(handle);
        TipoAlgoritmoDeConsenso algoritmoActual = coleccionInfo.getAlgoritmoDeConsenso();
        Hecho hecho = coleccionService.obtenerHechoPorId(hechoId);
        if (hecho == null) throw new NoSuchElementException("El hecho con id " + hechoId + " no existe.");
        return mapearConConsenso(hecho, algoritmoActual);
    }

    public PaginaDTO<CategoriaOutputDTO> obtenerPaginado(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Categoria> paginaEntidad = categoriaRepository.findAll(pageable);
        List<CategoriaOutputDTO> contenidoDTO = paginaEntidad.getContent().stream()
                .map(this::mapearADTO)
                .collect(Collectors.toList());

        PaginaDTO<CategoriaOutputDTO> paginaDTO = new PaginaDTO<>();
        paginaDTO.setContent(contenidoDTO);
        paginaDTO.setNumber(paginaEntidad.getNumber());
        paginaDTO.setSize(paginaEntidad.getSize());
        paginaDTO.setTotalElements(paginaEntidad.getTotalElements());
        paginaDTO.setTotalPages(paginaEntidad.getTotalPages());
        paginaDTO.setNumberOfElements(paginaEntidad.getNumberOfElements());
        paginaDTO.setFirst(paginaEntidad.isFirst());
        paginaDTO.setLast(paginaEntidad.isLast());
        return paginaDTO;
    }

    private CategoriaOutputDTO mapearADTO(Categoria entidad) {
        CategoriaOutputDTO dto = new CategoriaOutputDTO(entidad.getNombre());
        dto.setId(entidad.getId());
        return dto;
    }

    private String normalizar(String s) {
        if (s == null) return "";
        String sinTildes = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase();
    }

    public List<HechoOutputDTO> obtenerHechosConFiltro(FiltroHechosInput filtro) {
        List<HechoOutputDTO> hechos = this.obtenerHechos();
        if (filtro == null) return hechos;

        return hechos.stream()
                .filter(h -> filtro.getCategoria() == null || h.getCategoria().equalsIgnoreCase(filtro.getCategoria()))
                .filter(h -> filtro.getProvincia() == null || (h.getProvincia() != null && h.getProvincia().equalsIgnoreCase(filtro.getProvincia())))
                .filter(h -> filtro.getTituloContiene() == null || (h.getTitulo() != null && h.getTitulo().toLowerCase().contains(filtro.getTituloContiene().toLowerCase())))
                .filter(h -> {
                    if (filtro.getFechaDesde() == null && filtro.getFechaHasta() == null) return true;
                    if (h.getFechaAcontecimiento() == null) return false;
                    LocalDate fecha = h.getFechaAcontecimiento().toLocalDate();
                    boolean okDesde = filtro.getFechaDesde() == null || fecha.isAfter(LocalDate.parse(filtro.getFechaDesde()));
                    boolean okHasta = filtro.getFechaHasta() == null || fecha.isBefore(LocalDate.parse(filtro.getFechaHasta()));
                    return okDesde && okHasta;
                })
                .collect(Collectors.toList());
    }
}