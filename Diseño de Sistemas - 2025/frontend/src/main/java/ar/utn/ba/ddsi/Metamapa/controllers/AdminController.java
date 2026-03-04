package ar.utn.ba.ddsi.Metamapa.controllers;

import ar.utn.ba.ddsi.Metamapa.models.dtos.*;
import ar.utn.ba.ddsi.Metamapa.services.AdminService;
import ar.utn.ba.ddsi.Metamapa.services.ColeccionService;
import ar.utn.ba.ddsi.Metamapa.services.HechoService;
import ar.utn.ba.ddsi.Metamapa.services.MetaMapaApiService;
import ar.utn.ba.ddsi.Metamapa.services.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final ColeccionService coleccionService;
    private final SolicitudService solicitudService;
    private final HechoService hechoService;
    private final AdminService adminService;
    private final MetaMapaApiService metamapaApiService;

    @GetMapping("/panel-control")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String mostrarPanelControl(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        ResumenDTO resumen = metamapaApiService.getPanelDeControl();

        PaginaDTO<ColeccionDTO> resp = this.coleccionService.getColeccionesPaginadas(page, size, keyword);

        long from = resp.getNumber() * (long) resp.getSize() + (resp.getNumberOfElements() > 0 ? 1 : 0);
        long to   = resp.getNumber() * (long) resp.getSize() + resp.getNumberOfElements();

        model.addAttribute("resumen", resumen);
        model.addAttribute("titulo", "Panel de Control");
        model.addAttribute("colecciones", resp.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("page", resp.getNumber());
        model.addAttribute("size", resp.getSize());
        model.addAttribute("totalPages", resp.getTotalPages());
        model.addAttribute("totalElements", resp.getTotalElements());
        model.addAttribute("hasPrev", !resp.isFirst());
        model.addAttribute("hasNext", !resp.isLast());
        model.addAttribute("prevPage", resp.getNumber() - 1);
        model.addAttribute("nextPage", resp.getNumber() + 1);
        model.addAttribute("from", from);
        model.addAttribute("to", to);

        return "administrador/panelControl";
    }

    @GetMapping("/gestor-solicitudes")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String mostrarGestorSolicitudes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long idSolicitud,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Model model
    ) {
        PaginaDTO<SolicitudDTO> resp = adminService.obtenerSolicitudesPaginado(page, size, idSolicitud, estado, fecha);

        long from = resp.getNumber() * (long) resp.getSize() + (resp.getNumberOfElements() > 0 ? 1 : 0);
        long to   = resp.getNumber() * (long) resp.getSize() + resp.getNumberOfElements();

        model.addAttribute("titulo", "Gestor de Solicitudes");
        model.addAttribute("solicitudes", resp.getContent());

        model.addAttribute("filtroId", idSolicitud);
        model.addAttribute("filtroEstado", estado);
        model.addAttribute("filtroFecha", fecha);

        model.addAttribute("page", resp.getNumber());
        model.addAttribute("size", resp.getSize());
        model.addAttribute("totalPages", resp.getTotalPages());
        model.addAttribute("totalElements", resp.getTotalElements());
        model.addAttribute("hasPrev", !resp.isFirst());
        model.addAttribute("hasNext", !resp.isLast());
        model.addAttribute("prevPage", resp.getNumber() - 1);
        model.addAttribute("nextPage", resp.getNumber() + 1);
        model.addAttribute("from", from);
        model.addAttribute("to", to);

        return "administrador/gestorSolicitudes";
    }

    @GetMapping("/gestor-hechos")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String mostrarGestorHechos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,

            @RequestParam(required = false) Long idHecho,
            @RequestParam(required = false) String ubicacion,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,

            // --- NUEVOS PARÁMETROS GEOESPACIALES ---
            @RequestParam(required = false) Double latitud,
            @RequestParam(required = false) Double longitud,
            @RequestParam(required = false) Double radio,
            // ----------------------------------------

            @RequestParam(defaultValue = "0") int catPage,
            @RequestParam(defaultValue = "5") int catSize,
            Model model
    ) {

        // Llamada actualizada al servicio pasando coordenadas y radio
        PaginaDTO<HechoDTO> respHechos = adminService.obtenerHechosPaginado(
                page, size, idHecho, ubicacion, estado, fecha, latitud, longitud, radio
        );

        long from = respHechos.getNumber() * (long) respHechos.getSize() + (respHechos.getNumberOfElements() > 0 ? 1 : 0);
        long to   = respHechos.getNumber() * (long) respHechos.getSize() + respHechos.getNumberOfElements();

        model.addAttribute("hechos", respHechos.getContent());

        // Filtros originales al modelo
        model.addAttribute("filtroId", idHecho);
        model.addAttribute("filtroUbicacion", ubicacion);
        model.addAttribute("filtroEstado", estado);
        model.addAttribute("filtroFecha", fecha);

        // --- NUEVOS FILTROS AL MODELO (Importante para mantener estado en la vista) ---
        model.addAttribute("filtroLat", latitud);
        model.addAttribute("filtroLon", longitud);
        model.addAttribute("filtroRadio", radio);
        // -----------------------------------------------------------------------------

        model.addAttribute("page", respHechos.getNumber());
        model.addAttribute("size", respHechos.getSize());
        model.addAttribute("totalPages", respHechos.getTotalPages());
        model.addAttribute("totalElements", respHechos.getTotalElements());
        model.addAttribute("hasPrev", !respHechos.isFirst());
        model.addAttribute("hasNext", !respHechos.isLast());
        model.addAttribute("prevPage", respHechos.getNumber() - 1);
        model.addAttribute("nextPage", respHechos.getNumber() + 1);
        model.addAttribute("from", from);
        model.addAttribute("to", to);

        // Paginación de Categorías (se mantiene igual)
        PaginaDTO<CategoriaDTO> respCat = adminService.obtenerCategoriasPaginado(catPage, catSize);
        model.addAttribute("categorias", respCat.getContent());
        model.addAttribute("catPage", respCat.getNumber());
        model.addAttribute("catSize", respCat.getSize());
        model.addAttribute("catTotalPages", respCat.getTotalPages());
        model.addAttribute("catTotalElements", respCat.getTotalElements());
        model.addAttribute("catHasPrev", !respCat.isFirst());
        model.addAttribute("catHasNext", !respCat.isLast());
        model.addAttribute("catPrevPage", respCat.getNumber() - 1);
        model.addAttribute("catNextPage", respCat.getNumber() + 1);

        model.addAttribute("titulo", "Gestor de Hechos");
        return "administrador/gestorHechos";
    }

    @GetMapping("/importarCSV")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String verImportadorCSV(Model model) {
        model.addAttribute("titulo", "Importacion de hechos en archivos CSV");
        return "administrador/importadorArchivosCSV";
    }

    @PostMapping("/importarCSV")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String importarCSV(@RequestParam("archivo") MultipartFile archivo, RedirectAttributes redirect){
        // Aquí dejamos el try-catch porque es una lógica muy específica de UI con mensajes flash
        // que no se traduce bien a una página de error 500 genérica.
        if(archivo == null || archivo.isEmpty()) {
            redirect.addFlashAttribute("error", "Selecciona un archivo CSV");
            return "redirect:/admin/importarCSV";
        }
        try {
            InformeDeResultadosDTO informe = this.metamapaApiService.importarHechosCsv(archivo);
            redirect.addFlashAttribute("mensaje",
                    "Importación OK: " + informe.getGuardadosTotales() + "/" + informe.getHechosTotales() + " guardados (" + informe.getTiempoTardado() + " ms)");
            redirect.addFlashAttribute("tipoMensaje", "success");
        } catch (Exception e) {
            redirect.addFlashAttribute("mensaje", "No se pudo importar el CSV: " + e.getMessage());
            redirect.addFlashAttribute("tipoMensaje", "error");
        }
        return "redirect:/admin/importarCSV";
    }

    @PostMapping("coleccion/{handle}/eliminar")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String eliminarColeccion(@PathVariable String handle, RedirectAttributes redirectAttributes) {
        // Sin try-catch. Si no existe la colección -> 404 Page.
        coleccionService.eliminarColeccion(handle);
        redirectAttributes.addFlashAttribute("mensaje", "Colección eliminada exitosamente");
        redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/admin/panel-control";
    }

    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String aprobarSolicitud(@PathVariable Long id, RedirectAttributes redirectAttributes){
        // Sin try-catch.
        solicitudService.aprobarSolicitud(id);
        redirectAttributes.addFlashAttribute("mensaje", "Solicitud aprobada exitosamente");
        redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/admin/gestor-solicitudes";
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String rechazarSolicitud(@PathVariable Long id, RedirectAttributes redirectAttributes){
        // Sin try-catch.
        solicitudService.rechazarSolicitud(id);
        redirectAttributes.addFlashAttribute("mensaje", "Solicitud rechazada exitosamente");
        redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/admin/gestor-solicitudes";
    }

    @PostMapping("hecho/{id}/eliminar")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String eliminarHecho(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Sin try-catch.
        hechoService.eliminarHecho(id);
        redirectAttributes.addFlashAttribute("mensaje", "Hecho eliminado exitosamente");
        redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/admin/gestor-hechos";
    }

    @DeleteMapping("/categorias/{id}/eliminar")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String eliminarCategoria(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Sin try-catch.
        adminService.eliminarCategoria(id);
        redirectAttributes.addFlashAttribute("mensaje", "Categoria eliminada exitosamente");
        redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/admin/gestor-hechos";
    }

    @GetMapping("/coleccion/{handle}/editar")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String editarColeccionForm(@PathVariable String handle, Model model) {
        // IMPORTANTE: Aquí eliminé el try-catch con redirección manual a /404
        ColeccionDTO coleccion = coleccionService.getColeccionByHandle(handle);

        model.addAttribute("titulo", "Editar Colección");
        model.addAttribute("coleccion", coleccion);
        model.addAttribute("listaCategorias", metamapaApiService.getCategorias());
        model.addAttribute("listaOrigenes", List.of("OFICIAL", "ONG", "CIUDADANA"));
        model.addAttribute("algoritmosDisponibles", List.of("CONSENSO_ABSOLUTO", "MAYORIA_SIMPLE", "MULTIPLES_MENCIONES"));

        return "administrador/editarColeccion";
    }

    @PostMapping("/coleccion/{handle}/editar")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String editarColeccionGuardar(@PathVariable String handle,
                                         @ModelAttribute("coleccion") ColeccionDTO form,
                                         BindingResult binding,
                                         RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            redirect.addFlashAttribute("mensaje", "Revisá los campos del formulario.");
            redirect.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/admin/coleccion/" + handle + "/editar";
        }
        // Sin try-catch. Si no existe la colección o hay error de servidor -> Página de Error
        form.setHandle(handle);
        metamapaApiService.actualizarColeccion(handle, form);

        redirect.addFlashAttribute("mensaje", "Colección editada correctamente. Verás los hechos en la próxima actualización de colecciones.");
        redirect.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/admin/panel-control";
    }

    @PostMapping("/categorias/{id}/editar")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String editarCategoria(@PathVariable Long id,
                                  @ModelAttribute("categoria") CategoriaDTO form,
                                  BindingResult binding,
                                  RedirectAttributes redirect) {
        if (binding.hasErrors()) {
            redirect.addFlashAttribute("mensaje", "Revisá los campos.");
            redirect.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/admin/gestor-hechos";
        }
        // Sin try-catch.
        adminService.actualizarCategoria(id, form);
        redirect.addFlashAttribute("mensaje", "Categoria actualizada correctamente.");
        redirect.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/admin/gestor-hechos";
    }

    @PostMapping("/categorias/nueva")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String crearCategoria(@ModelAttribute("categoria") CategoriaDTO categoria, RedirectAttributes redirect){
        // Sin try-catch.
        CategoriaDTO creada = adminService.crearCategoria(categoria);
        redirect.addFlashAttribute("mensaje", "Categoria creada: " + creada.getNombre());
        return "redirect:/admin/gestor-hechos";
    }

    @GetMapping("/estadisticas")
    @PreAuthorize("hasRole('ADMIN')")
    public String verDashboard(@RequestParam(defaultValue = "anio") String rango, Model model) {
        // Sin try-catch.
        DashboardDTO stats = adminService.obtenerEstadisticas(rango);

        Map<String, Object> kpis = new HashMap<>();
        kpis.put("totalHechos", stats.getTotalHechos() != null ? stats.getTotalHechos() : 0);
        kpis.put("hechosVerificados", stats.getHechosVerificados() != null ? stats.getHechosVerificados() : 0);
        kpis.put("spamDetectado", stats.getSpamDetectado() != null ? stats.getSpamDetectado() : 0);
        kpis.put("porcentajeSpam", stats.getPorcentajeSpam() != null ? String.format("%.1f", stats.getPorcentajeSpam()) : "0.0");

        model.addAttribute("titulo", "Panel de Estadísticas");
        model.addAttribute("kpis", kpis);
        model.addAttribute("resumen", stats);
        model.addAttribute("datosCategorias", stats.getHechosPorCategoria() != null ? stats.getHechosPorCategoria() : Map.of());
        model.addAttribute("datosProvincias", stats.getHechosPorProvincia() != null ? stats.getHechosPorProvincia() : Map.of());
        model.addAttribute("datosHorarios", stats.getHechosPorHora() != null ? stats.getHechosPorHora() : Map.of());

        return "administrador/estadisticas";
    }

    @GetMapping("/exportar")
    public ResponseEntity<Resource> exportarDatos() {
        ByteArrayResource resource = adminService.exportarCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=metamapa_stats.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }

    @GetMapping("/solicitudes-edicion")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String verSolicitudesEdicionCreacion(Model model) {
        // Sin try-catch.
        List<SolicitudEdicionCreacionDTO> solicitudes = solicitudService.getSolicitudesEdicionCreacion();

        model.addAttribute("titulo", "Solicitudes de Creación y Edición");
        model.addAttribute("solicitudes", solicitudes);

        long pendientesCreacion = solicitudes.stream().filter(s -> "CREACION".equals(s.getTipoSolicitud())).count();
        long pendientesEdicion = solicitudes.stream().filter(s -> "EDICION".equals(s.getTipoSolicitud())).count();

        model.addAttribute("countCreacion", pendientesCreacion);
        model.addAttribute("countEdicion", pendientesEdicion);

        return "administrador/gestorSolicitudesEdicion";
    }

    @PostMapping("/solicitudes-edicion/{id}/aceptar")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String aceptarSolicitudEdicion(@PathVariable Long id, RedirectAttributes redirect) {
        // Sin try-catch.
        solicitudService.aceptarSolicitudEdicionCreacion(id);
        redirect.addFlashAttribute("mensaje", "Solicitud ACEPTADA correctamente.");
        redirect.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/admin/solicitudes-edicion";
    }

    @PostMapping("/solicitudes-edicion/{id}/rechazar")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public String rechazarSolicitudEdicion(@PathVariable Long id, RedirectAttributes redirect) {
        // Sin try-catch.
        solicitudService.rechazarSolicitudEdicionCreacion(id);
        redirect.addFlashAttribute("mensaje", "Solicitud RECHAZADA correctamente.");
        redirect.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/admin/solicitudes-edicion";
    }
}