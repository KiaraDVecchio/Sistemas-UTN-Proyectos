package ar.utn.ba.ddsi.controllers.impl;

import ar.utn.ba.ddsi.controllers.IEstadisticaController;
import ar.utn.ba.ddsi.models.dtos.output.DashboardDTO;
import ar.utn.ba.ddsi.models.entities.Estadistica;
import ar.utn.ba.ddsi.models.repositories.IEstadisticaRepository;
import ar.utn.ba.ddsi.services.IEstadisticaService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/estadisticas")
public class EstadisticaController implements IEstadisticaController {

    private final IEstadisticaService estadisticasService;
    private final IEstadisticaRepository repo;

    @Autowired
    public EstadisticaController(IEstadisticaService estadisticasService,
                                 IEstadisticaRepository repo) {
        this.estadisticasService = estadisticasService;
        this.repo = repo;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> obtenerDashboard(@RequestParam(defaultValue = "anio") String rango) {
        DashboardDTO dashboard = new DashboardDTO();

        dashboard.setTotalHechos(0L); dashboard.setHechosVerificados(0L);
        dashboard.setSpamDetectado(0L); dashboard.setPorcentajeSpam(0.0);
        dashboard.setHechosPorCategoria(new HashMap<>());
        dashboard.setHechosPorProvincia(new HashMap<>());
        dashboard.setHechosPorHora(new HashMap<>());
        dashboard.setDetallesPorCategoria(new ArrayList<>());


        repo.findTopByTotalHechosIsNotNullOrderByFechaDeCalculoDesc()
                .ifPresent(stat -> {
                    // Mapeo directo y simple
                    dashboard.setTotalHechos(stat.getTotalHechos());
                    dashboard.setHechosVerificados(stat.getHechosVerificados());

                    if (stat.getSolicitudesEliminacionSpam() != null) {
                        var s = stat.getSolicitudesEliminacionSpam();
                        dashboard.setSpamDetectado(s.getSpam());
                        dashboard.setPorcentajeSpam(s.getTotal() > 0 ? (double) s.getSpam() / s.getTotal() * 100 : 0.0);
                    }
                    if (stat.getCategoriaTopGlobal() != null) {
                        dashboard.setHechosPorCategoria(stat.getCategoriaTopGlobal().getCantidadPorCategoria());
                    }
                    if (stat.getHechosPorProvinciaEnColeccion() != null) {
                        dashboard.setHechosPorProvincia(stat.getHechosPorProvinciaEnColeccion().getCantidadPorProvincia());
                    }
                    if (stat.getHorarioPicoPorCategoria() != null) {
                        dashboard.setHechosPorHora(stat.getHorarioPicoPorCategoria().getCantidadPorHora());
                    }
                });

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public void exportarCSV(HttpServletResponse resp) throws IOException {
        resp.setHeader("Content-Disposition", "attachment; filename=estadisticas.csv");
        List<Estadistica> todas = repo.findAll();

        try (PrintWriter w = resp.getWriter()) {
            w.println("id,fecha,tipo,detalle,ganador,cantidad,datos");

            for (Estadistica e : todas) {
                var hp = e.getHechosPorProvinciaEnColeccion();
                if (hp != null) escribirFila(w, e, "HECHOS_POR_PROVINCIA_COLECCION", hp.getColeccionHandle(), null, null, hp.getProvinciaGanadora(), hp.getCantidadGanadora(), mapALinea(hp.getCantidadPorProvincia()));

                var pt = e.getProvinciaTopPorCategoria();
                if (pt != null) escribirFila(w, e, "PROVINCIA_TOP_POR_CATEGORIA", null, nvl(pt.getCategoria()), null, pt.getProvinciaGanadora(), pt.getCantidadGanadora(), mapALinea(pt.getCantidadPorProvincia()));

                var hpico = e.getHorarioPicoPorCategoria();
                if (hpico != null) escribirFila(w, e, "HORARIO_PICO_POR_CATEGORIA", null, nvl(hpico.getCategoria()), hpico.getHoraGanadora(), hpico.getHoraGanadora() != null ? hpico.getHoraGanadora().toString() : "", hpico.getCantidadGanadora(), mapHorasALinea(hpico.getCantidadPorHora()));

                var top = e.getCategoriaTopGlobal();
                if (top != null) escribirFila(w, e, "TOP_CATEGORIA_GLOBAL", null, null, null, nvl(top.getCategoriaGanadora()), top.getCantidadGanadora(), mapALinea(top.getCantidadPorCategoria()));

                var sol = e.getSolicitudesEliminacionSpam();
                if (sol != null) {
                    escribirFila(w, e, "SOLICITUDES_ELIMINACION_TOTAL", null, null, null, "total", nvl(sol.getTotal()), null);
                    escribirFila(w, e, "SOLICITUDES_ELIMINACION_SPAM", null, null, null, "spam", nvl(sol.getSpam()), null);
                }
            }
        }
    }

    private void escribirFila(PrintWriter w, Estadistica e, String tipo, String col, String cat, Integer h, String gan, Long cant, String brk) {
        w.printf("%d,%s,%s,%s,%s,%s,%s,%s,%s%n", e.getId(), e.getFechaDeCalculo(), csv(tipo), csv(nvl(col)), csv(nvl(cat)), h==null?"":h.toString(), csv(nvl(gan)), cant==null?"":cant.toString(), csv(nvl(brk)));
    }
    private String nvl(String s) { return s == null ? "" : s; }
    private Long nvl(Long v) { return v == null ? 0L : v; }
    private String csv(String s) { if (s == null || s.isEmpty()) return ""; return s.contains(",") ? "\"" + s.replace("\"", "\"\"") + "\"" : s; }
    private String mapALinea(Map<String, Long> m) { if (m == null) return ""; return m.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(";")); }
    private String mapHorasALinea(Map<Integer, Long> m) { if (m == null) return ""; return m.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(";")); }
}