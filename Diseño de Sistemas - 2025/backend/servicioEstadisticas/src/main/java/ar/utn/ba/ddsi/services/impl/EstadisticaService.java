package ar.utn.ba.ddsi.services.impl;

import ar.utn.ba.ddsi.models.dtos.output.HechoOutputDTO;
import ar.utn.ba.ddsi.models.entities.*;
import ar.utn.ba.ddsi.models.repositories.IEstadisticaRepository;
import ar.utn.ba.ddsi.services.IEstadisticaService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap; // IMPORTANTE
import java.util.stream.Collectors;

@Service
public class EstadisticaService implements IEstadisticaService {

    private final WebClient webClient;
    private final IEstadisticaRepository estadisticaRepository;
    private final String agregadorUrl = "https://agregador-production-0577.up.railway.app/api/public";

    // 1. CACHÉ PERSISTENTE (CONCURRENT PARA SOPORTAR PARALELISMO)
    // Al estar declarado acá, no se borra cuando termina el método.
    private final Map<String, String> cacheGeoref = new ConcurrentHashMap<>();

    @Autowired
    public EstadisticaService(IEstadisticaRepository estadisticaRepository,
                              WebClient.Builder webClientBuilder) {
        this.estadisticaRepository = estadisticaRepository;
        // Aumentamos buffer por si la respuesta del agregador es gigante
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(32 * 1024 * 1024))
                .build();
        this.webClient = webClientBuilder.exchangeStrategies(strategies).build();
    }

    @Override
    public void recalcularEstadisticas() {
        long start = System.currentTimeMillis();
        System.out.println(">>> INICIANDO CÁLCULO ESTADÍSTICAS (PARALELO + CACHÉ PERSISTENTE)...");

        LocalDateTime ahora = LocalDateTime.now();

        // 1. Obtener datos crudos
        List<HechoOutputDTO> dtosRaw = this.obtenerHechosOutputDTO();
        List<SolicitudDeEliminacion> solicitudes = this.obtenerSolicitudes();

        if (dtosRaw.isEmpty()) return;

        System.out.println(">>> Datos obtenidos. Procesando " + dtosRaw.size() + " hechos...");

        // 2. PROCESAMIENTO PARALELO
        // Usamos parallelStream() para usar todos los núcleos del CPU
        List<Hecho> todosLosHechos = dtosRaw.parallelStream()
                .map(this::convertirAEntidad) // Ya no pasamos el mapa, usamos el de la clase
                .collect(Collectors.toList());

        long duration = System.currentTimeMillis() - start;
        System.out.println(">>> PROCESAMIENTO FINALIZADO en " + (duration/1000) + " segundos.");
        System.out.println(">>> Tamaño del Caché actual: " + cacheGeoref.size() + " ubicaciones únicas.");

        // 3. Creación del Snapshot (Igual que antes)
        Estadistica snapshot = new Estadistica();
        snapshot.setFechaDeCalculo(ahora);

        snapshot.setTotalHechos((long) todosLosHechos.size());
        snapshot.setHechosVerificados(todosLosHechos.stream()
                .filter(h -> !Boolean.TRUE.equals(h.getFueEliminado())).count());

        if (!solicitudes.isEmpty()) {
            long spam = solicitudes.stream().filter(s -> Boolean.TRUE.equals(s.getEsSpam())).count();
            SolicitudesEliminacionSpam subSpam = new SolicitudesEliminacionSpam();
            subSpam.setTotal((long) solicitudes.size());
            subSpam.setSpam(spam);
            snapshot.setSolicitudesEliminacionSpam(subSpam);
        }

        // Dona de Categorías
        Map<String, Long> globalCat = todosLosHechos.stream()
                .filter(h -> !Boolean.TRUE.equals(h.getFueEliminado()) && h.getCategoria() != null)
                .collect(Collectors.groupingBy(h -> h.getCategoria().getNombre().trim().toLowerCase(), Collectors.counting()));

        globalCat.entrySet().stream().max(Map.Entry.comparingByValue()).ifPresent(top -> {
            CategoriaTopGlobal ctg = new CategoriaTopGlobal();
            ctg.setCategoriaGanadora(top.getKey());
            ctg.setCantidadGanadora(top.getValue());
            ctg.setCantidadPorCategoria(globalCat);
            snapshot.setCategoriaTopGlobal(ctg);
        });

        // Provincias
        Map<String, Long> globalProv = todosLosHechos.stream()
                .filter(h -> h.getUbicacion() != null && h.getUbicacion().getProvincia() != null)
                .collect(Collectors.groupingBy(h -> h.getUbicacion().getProvincia(), Collectors.counting()));

        if (!globalProv.isEmpty()) {
            Map.Entry<String, Long> top = globalProv.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow();
            HechosPorProvinciaEnColeccion sub = new HechosPorProvinciaEnColeccion();
            sub.setColeccionHandle("GLOBAL");
            sub.setProvinciaGanadora(top.getKey());
            sub.setCantidadGanadora(top.getValue());
            sub.setCantidadPorProvincia(globalProv);
            snapshot.setHechosPorProvinciaEnColeccion(sub);
        }

        // Horarios
        Map<Integer, Long> globalHoras = todosLosHechos.stream()
                .filter(h -> h.getFechaYHoraAcontecimiento() != null)
                .collect(Collectors.groupingBy(h -> h.getFechaYHoraAcontecimiento().getHour(), Collectors.counting()));

        if (!globalHoras.isEmpty()) {
            Map.Entry<Integer, Long> top = globalHoras.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow();
            HorarioPicoPorCategoria sub = new HorarioPicoPorCategoria();
            sub.setCategoria("GLOBAL");
            sub.setHoraGanadora(top.getKey());
            sub.setCantidadGanadora(top.getValue());
            sub.setCantidadPorHora(globalHoras);
            snapshot.setHorarioPicoPorCategoria(sub);
        }

        estadisticaRepository.save(snapshot);
        System.out.println(">>> SNAPSHOT GUARDADO.");
    }

    // --- Métodos Auxiliares ---

    private List<HechoOutputDTO> obtenerHechosOutputDTO() {
        try {
            List<HechoOutputDTO> dtos = webClient.get().uri(agregadorUrl + "/hechos")
                    .retrieve().bodyToFlux(HechoOutputDTO.class).collectList().block();
            return dtos == null ? List.of() : dtos;
        } catch (Exception e) { return List.of(); }
    }

    public List<SolicitudDeEliminacion> obtenerSolicitudes() {
        try {
            return webClient.get().uri(agregadorUrl + "/solicitudes")
                    .retrieve().bodyToFlux(SolicitudDeEliminacion.class).collectList().block();
        } catch (Exception e) { return List.of(); }
    }

    private Hecho convertirAEntidad(HechoOutputDTO dto) {
        Hecho h = new Hecho();
        h.setTitulo(dto.getTitulo());
        h.setDescripcion(dto.getDescripcion());
        h.setFechaYHoraAcontecimiento(dto.getFechaAcontecimiento());
        h.setFueEliminado(dto.getFueEliminado());

        if(dto.getCategoria() != null) {
            Categoria c = new Categoria();
            c.setNombre(dto.getCategoria());
            h.setCategoria(c);
        }

        Ubicacion u = new Ubicacion();
        boolean ubicacionSeteada = false;

        // Lógica Optimizada
        if (dto.getProvincia() != null && !dto.getProvincia().isEmpty()) {
            u.setProvincia(dto.getProvincia());
            ubicacionSeteada = true;
        }
        else if (dto.getLatitud() != null && dto.getLongitud() != null) {
            String key = dto.getLatitud() + "," + dto.getLongitud();

            // 1. Buscamos en caché de MEMORIA (Rapidísimo)
            if (this.cacheGeoref.containsKey(key)) {
                u.setProvincia(this.cacheGeoref.get(key));
                ubicacionSeteada = true;
            } else {
                // 2. Si no está, llamamos a API (Lento, pero solo ocurre una vez por coordenada única)
                String provinciaDetectada = obtenerProvinciaGeoref(dto.getLatitud(), dto.getLongitud());
                if (provinciaDetectada != null) {
                    u.setProvincia(provinciaDetectada);
                    this.cacheGeoref.put(key, provinciaDetectada); // Guardamos para siempre (mientras corra la app)
                    ubicacionSeteada = true;
                }
            }
        }

        if (ubicacionSeteada) {
            h.setUbicacion(u);
        }

        return h;
    }

    private String obtenerProvinciaGeoref(Double lat, Double lon) {
        try {
            // Quitamos el Thread.sleep para maximizar velocidad en paralelo
            GeorefResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("apis.datos.gob.ar")
                            .path("/georef/api/ubicacion")
                            .queryParam("lat", lat)
                            .queryParam("lon", lon)
                            .build())
                    .retrieve()
                    .bodyToMono(GeorefResponse.class)
                    .block(); // Bloqueo intencional dentro del thread paralelo

            if (response != null && response.getUbicacion() != null
                    && response.getUbicacion().getProvincia() != null) {
                return response.getUbicacion().getProvincia().getNombre();
            }
        } catch (Exception e) {
            // Fallo silencioso para no detener el proceso masivo
        }
        return null;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeorefResponse {
        private GeorefUbicacion ubicacion;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeorefUbicacion {
        private GeorefProvincia provincia;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeorefProvincia {
        private String id;
        private String nombre;
    }
}