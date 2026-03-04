package ar.utn.ba.ddsi.adapters;

import ar.utn.ba.ddsi.models.dtos.input.HechoProxyInputDTO;
import ar.utn.ba.ddsi.models.entities.Categoria;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.Ubicacion;
import ar.utn.ba.ddsi.models.entities.enumerados.Origen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class AdapterFuenteProxy {

    private final WebClient webClient;
    private AtomicInteger ultimaPaginaProcesada = new AtomicInteger(1);

    @Autowired
    public AdapterFuenteProxy(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }


    public List<Hecho> obtenerHechos(String fuenteUrl) {
        List<Hecho> todosLosHechos = new ArrayList<>();

        int size = 10;
        int paginasPorEjecucion = 50;

        int paginasProcesadasHoy = 0;
        boolean hayMasDatos = true;

        System.out.println("🔄 Retomando descarga desde página: " + ultimaPaginaProcesada.get());

        while (hayMasDatos && paginasProcesadasHoy < paginasPorEjecucion) {
            int paginaActual = ultimaPaginaProcesada.get();

            try {
                String urlPaginada = UriComponentsBuilder.fromHttpUrl(fuenteUrl)
                        .queryParam("page", paginaActual)
                        .queryParam("size", size)
                        .toUriString();

                System.out.println("   ⬇️ Bajando Pág " + paginaActual + "...");

                List<HechoProxyInputDTO> paginaDTOs = webClient.get()
                        .uri(urlPaginada)
                        .retrieve()
                        .bodyToFlux(HechoProxyInputDTO.class)
                        .collectList()
                        .block();

                if (paginaDTOs == null || paginaDTOs.isEmpty()) {

                    System.out.println("🏁 Fin de la API detectado. Reiniciando ciclo.");
                    ultimaPaginaProcesada.set(1); // Reseteamos para la próxima vez empezar de cero
                    hayMasDatos = false;
                } else {
                    List<Hecho> hechosPagina = paginaDTOs.stream()
                            .map(this::mapToEntity)
                            .collect(Collectors.toList());

                    todosLosHechos.addAll(hechosPagina);

                    ultimaPaginaProcesada.incrementAndGet();
                    paginasProcesadasHoy++;

                    if (paginaDTOs.size() < size) {
                        System.out.println("🏁 Última página incompleta detectada. Reiniciando ciclo.");
                        ultimaPaginaProcesada.set(1);
                        hayMasDatos = false;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error en página " + paginaActual + ": " + e.getMessage());

                hayMasDatos = false;
            }
        }

        System.out.println("✅ Lote finalizado. Procesados: " + todosLosHechos.size() + " hechos. Próxima ejecución inicia en pág: " + ultimaPaginaProcesada.get());
        return todosLosHechos;
    }

    private Hecho mapToEntity(HechoProxyInputDTO dto) {
        Hecho hecho = new Hecho();

        hecho.setTitulo(dto.getTitulo());
        hecho.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion() : "Sin descripción disponible");
        if (dto.getCategoria() != null) {
            hecho.setCategoria(new Categoria(dto.getCategoria()));
        } else {
            hecho.setCategoria(new Categoria("Sin categoría"));
        }

        if (dto.getFechaAcontecimiento() != null) {
            hecho.setFechaAcontecimiento(dto.getFechaAcontecimiento().atStartOfDay());
        } else {
            hecho.setFechaAcontecimiento(LocalDateTime.now());
        }

        hecho.setOrigen(Origen.PROXY);

        hecho.setFechaCarga(java.time.LocalDate.now());
        hecho.setFueEliminado(false);

        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setLatitud(dto.getLatitud());
        ubicacion.setLongitud(dto.getLongitud());

        hecho.setUbicacion(ubicacion);

        return hecho;
    }
}