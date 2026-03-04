package ar.utn.ba.ddsi.Metamapa.controllers;

import ar.utn.ba.ddsi.Metamapa.models.dtos.HechoDTO;
import ar.utn.ba.ddsi.Metamapa.models.dtos.SolicitudDTO;
import ar.utn.ba.ddsi.Metamapa.services.HechoService;
import ar.utn.ba.ddsi.Metamapa.services.SolicitudService;
import ar.utn.ba.ddsi.Metamapa.exceptions.NotFoundException; // Asegurate de importar tu excepción
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class SolicitudesController {
    private final SolicitudService solicitudService;
    private final HechoService hechoService;

    @GetMapping("/solicitud/{hechoId}")
    public String mostrarFormulario(@PathVariable Long hechoId, Model model) {
        HechoDTO hecho = hechoService.getHechoPorId(hechoId);
        // Validamos lanzando excepcion para que lo atrape el handler
        if (hecho == null) {
            throw new NoSuchElementException("No se encontró el hecho con id " + hechoId);
        }

        model.addAttribute("titulo", "Solicitar eliminación de hecho");
        model.addAttribute("hecho", hecho);

        if (!model.containsAttribute("justificacion")) {
            model.addAttribute("justificacion", "");
        }
        return "hechosYColecciones/solicitudEliminacion";
    }

    @GetMapping("/detalle/{solicitudId}")
    public String verDetalleSolicitud(@PathVariable Long solicitudId, Model model){
        SolicitudDTO solicitud = solicitudService.obtenerSolicitud(solicitudId);

        if (solicitud == null) {
            throw new NoSuchElementException("No se encontró la solicitud con id " + solicitudId);
        }

        Long hechoId = solicitud.getHechoId();
        if (hechoId == null) {
            throw new IllegalStateException("La solicitud no contiene un hecho asociado");
        }

        HechoDTO hecho = hechoService.getHechoPorId(hechoId);
        if (hecho == null) {
            throw new NoSuchElementException("No se encontró el hecho con id " + hechoId);
        }

        model.addAttribute("titulo", "Solicitud de eliminación de hecho");
        model.addAttribute("solicitud", solicitud);
        model.addAttribute("hecho", hecho);

        return "administrador/detalleSolicitud";
    }

    @PostMapping("/solicitud/{hechoId}/crear")
    public String crear(@PathVariable Long hechoId,
                        @RequestParam("justificacion") String justificacion,
                        Model model,
                        RedirectAttributes flash) {

        String texto = (justificacion == null) ? "" : justificacion.trim();

        if (texto.length() < 500) {
            HechoDTO hecho = hechoService.getHechoPorId(hechoId);
            model.addAttribute("titulo", "Solicitar eliminación de hecho");
            model.addAttribute("hecho", hecho);
            model.addAttribute("hechoId", hechoId);
            model.addAttribute("justificacion", justificacion);
            // Esto usa el th:if="${error}" del HTML
            model.addAttribute("error", "La justificación debe tener al menos 500 caracteres. Actualmente: " + texto.length());
            return "hechosYColecciones/solicitudEliminacion";
        }

        SolicitudDTO solicitudCreada = solicitudService.crearSolicitudEliminacion(hechoId, texto);

        if (solicitudCreada.getEsSpam() || "RECHAZADA".equals(solicitudCreada.getEstado())) {

            flash.addFlashAttribute("error", "Tu solicitud fue rechazada automáticamente por nuestro sistema de detección de spam y contenido no válido.");
        } else {

            flash.addFlashAttribute("mensaje", "Solicitud creada correctamente. Quedó en estado pendiente de revisión.");
        }

        return "redirect:/solicitud/" + hechoId;
    }
}