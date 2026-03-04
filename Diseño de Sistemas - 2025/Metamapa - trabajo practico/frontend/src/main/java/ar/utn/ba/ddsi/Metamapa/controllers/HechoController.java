package ar.utn.ba.ddsi.Metamapa.controllers;

import ar.utn.ba.ddsi.Metamapa.models.dtos.CategoriaDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoDTO;
import ar.utn.ba.ddsi.Metamapa.services.ColeccionService;
import ar.utn.ba.ddsi.Metamapa.services.HechoService;
import ar.utn.ba.ddsi.Metamapa.services.MetaMapaApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/hechos")
@RequiredArgsConstructor
public class HechoController {
    @Value("${backend.origin}")
    private String backendOrigin;
    private final HechoService hechoService;
    private final MetaMapaApiService metaMapaApiService;
    private final ColeccionService coleccionService;

    @GetMapping("/{id}")
    public String verDetalleHecho(
            @PathVariable Long id,
            @RequestParam(required = false) String handle,
            Model model) {

        HechoDTO hecho;
        if (handle != null && !handle.isBlank()) {
            hecho = hechoService.getHechoDeColeccion(handle, id); // Lanza NotFoundException si falla
            model.addAttribute("coleccionHandle", handle);
            model.addAttribute("coleccionTitulo", "Volver a la colección");
        } else {
            hecho = hechoService.getHechoPorId(id); // Lanza NotFoundException si falla
        }

        List<String> nombresMultimedia = (hecho.getIdContenidoMultimedia() == null)
                ? List.of()
                : hecho.getIdContenidoMultimedia();

        model.addAttribute("hecho", hecho);
        model.addAttribute("nombresMultimedia", nombresMultimedia);
        model.addAttribute("extensionesImagen", List.of("jpg", "jpeg", "png", "gif", "webp"));
        model.addAttribute("backendOrigin", backendOrigin);
        model.addAttribute("titulo", "Hecho " + hecho.getTitulo());

        return "hechosYColecciones/detalleHecho";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CONTRIBUYENTE')")
    @GetMapping("/nuevo")
    public String verFormulario(Model model) {
        List<CategoriaDTO> categorias = this.coleccionService.getCategorias();
        model.addAttribute("titulo", "Subir hecho");
        model.addAttribute("hecho", new HechoDTO(null, null, null, null, null, null));
        model.addAttribute("categorias", categorias);
        return "hechosYColecciones/formularioHecho";
    }

    @PostMapping("/nuevo")
    public String procesarFormulario(@ModelAttribute("hecho") HechoDTO hecho,
                                     @RequestParam("fecha") String fecha,
                                     @RequestParam("hora") String hora,
                                     @RequestParam(name = "multimedia", required = false) MultipartFile[] multimedia,
                                     RedirectAttributes redirect, Model model) {

        // Validación básica
        boolean error = false;
        if (hecho.getTitulo() == null || hecho.getTitulo().isBlank()) error = true;
        if (hecho.getCategoria() == null || hecho.getCategoria().isBlank()) error = true;
        if (hecho.getDescripcion() == null || hecho.getDescripcion().isBlank()) error = true;
        if (hecho.getProvincia() == null || hecho.getProvincia().isBlank()) error = true;
        if (hecho.getLatitud() == null || hecho.getLongitud() == null) error = true;
        if (fecha == null || fecha.isBlank() || hora == null || hora.isBlank()) error = true;

        if (!error) {
            try {
                LocalDate d = LocalDate.parse(fecha);
                LocalTime t = LocalTime.parse(hora);
                hecho.setFechaAcontecimiento(LocalDateTime.of(d, t));
            } catch (Exception e) {
                error = true;
            }
        }

        if (error) {
            model.addAttribute("titulo", "Subir hecho");
            model.addAttribute("categorias", this.coleccionService.getCategorias());
            model.addAttribute("error", "Completar los campos obligatorios correctamente.");
            return "hechosYColecciones/formularioHecho";
        }

        Long usuarioId = metaMapaApiService.getUsuarioIdFromAccessToken();
        // Si subirHecho falla, el GlobalExceptionHandler lo atrapa (error 500)
        this.hechoService.subirHecho(hecho, multimedia, usuarioId);

        redirect.addFlashAttribute("mensaje", "Tu hecho ha sido creado correctamente. Lo verás en la próxima actualización de colecciones.");
        redirect.addFlashAttribute("tipoMensaje", "success");

        return "redirect:/hechos/nuevo";
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAnyRole('CONTRIBUYENTE', 'ADMIN')")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        // Si no existe, lanza excepción y muestra error 404
        HechoDTO hecho = this.metaMapaApiService.getHechoPorId(id);
        List<CategoriaDTO> categorias = this.metaMapaApiService.getCategorias();

        List<String> nombresMultimedia = hecho.getIdContenidoMultimedia() == null ? List.of()
                : hecho.getIdContenidoMultimedia().stream()
                .map(p -> {
                    if (p == null) return null;
                    if (p.startsWith("http")) return p;
                    if (p.contains("\\") || p.contains(":")) {
                        String fileName = p.substring(p.replace("\\", "/").lastIndexOf("/") + 1);
                        return "/uploads/" + fileName;
                    }
                    if (p.startsWith("/uploads/")) return p;
                    return "/uploads/" + p;
                })
                .filter(Objects::nonNull)
                .toList();

        model.addAttribute("nombresMultimedia", nombresMultimedia);
        model.addAttribute("hecho", hecho);
        model.addAttribute("categorias", categorias);
        model.addAttribute("hechoId", id);
        model.addAttribute("titulo", "Editar Hecho");
        model.addAttribute("mediaBaseUrl", "/uploads");
        model.addAttribute("extensionesImagen", List.of("jpg", "jpeg", "png", "gif", "webp"));
        model.addAttribute("backendOrigin", backendOrigin);
        return "contribuyente/editorHechos";
    }

    @PostMapping("/{id}/editar")
    @PreAuthorize("hasAnyRole('CONTRIBUYENTE', 'ADMIN')")
    public String procesarEdicion(
            @PathVariable Long id, @ModelAttribute("hecho") HechoDTO hecho,
            @RequestParam("fecha") String fecha, @RequestParam("hora") String hora,
            @RequestParam(name = "multimedia", required = false) MultipartFile[] multimedia,
            @RequestParam(name = "replaceMedia", defaultValue = "false") boolean replaceMedia,
            @RequestParam(value = "deleteExisting", required = false) List<String> deleteExisting,
            RedirectAttributes redirect) {

        hecho.setId(id);
        Long usuarioId = metaMapaApiService.getUsuarioIdFromAccessToken();

        if (fecha != null && !fecha.isBlank() && hora != null && !hora.isBlank()) {
            try {
                LocalDate d = LocalDate.parse(fecha);
                LocalTime t = LocalTime.parse(hora);
                hecho.setFechaAcontecimiento(LocalDateTime.of(d, t));
            } catch (Exception e) {
                redirect.addFlashAttribute("mensaje", "La fecha u hora ingresada no es válida.");
                redirect.addFlashAttribute("tipoMensaje", "danger");
                return "redirect:/hechos/" + id + "/editar";
            }
        }

        // Si actualizaHecho falla, va al Global Handler
        hechoService.actualizarHecho(
                hecho.getIdEnFuente(),
                hecho,
                multimedia,
                replaceMedia,
                deleteExisting == null ? List.of() : deleteExisting,
                usuarioId
        );

        redirect.addFlashAttribute("mensaje", "Su solicitud de modificación se ha enviado con éxito.");
        redirect.addFlashAttribute("tipoMensaje", "success");

        return "redirect:/hechos/" + id;
    }

    @PostMapping("/{id}/sumarVista")
    public String sumarVistaHecho(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Si no existe, lanza excepción -> 404
        hechoService.sumarVistaHecho(id);
        redirectAttributes.addFlashAttribute("mensaje", "Se sumó una vista al hecho exitosamente");
        redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        return "redirect:/hechos/" + id;
    }

    @GetMapping("/mis-hechos")
    public String verMisHechos(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String estado,
            Model model) {

        Long usuarioId = metaMapaApiService.getUsuarioIdFromAccessToken();
        List<HechoDTO> hechos = hechoService.getMisHechosFiltrado(usuarioId, titulo, categoria, estado);
        List<CategoriaDTO> categorias = coleccionService.getCategorias();

        model.addAttribute("hechos", hechos);
        model.addAttribute("categorias", categorias);
        model.addAttribute("titulo", "Mis Hechos");
        model.addAttribute("descripcion", "Gestiona los hechos que has reportado.");

        Map<String, String> param = new HashMap<>();
        param.put("titulo", titulo);
        param.put("categoria", categoria);
        param.put("estado", estado);
        model.addAttribute("param", param);

        return "contribuyente/misHechos";
    }

    @GetMapping("/vista-previa/{idEnFuente}")
    @PreAuthorize("hasRole('ADMIN')")
    public String previsualizarHechoDeFuente(@PathVariable Long idEnFuente) {
        // Si no se encuentra, 404 directo desde el handler
        Long idLocal = metaMapaApiService.getIdLocalPorIdFuente(idEnFuente);
        return "redirect:/hechos/" + idLocal;
    }
}