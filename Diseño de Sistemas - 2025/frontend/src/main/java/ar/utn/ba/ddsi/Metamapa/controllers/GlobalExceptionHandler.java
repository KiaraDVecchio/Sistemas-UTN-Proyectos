package ar.utn.ba.ddsi.Metamapa.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFound(NoResourceFoundException e, Model model) {
        model.addAttribute("status", 404);
        model.addAttribute("error", "Página no encontrada");
        model.addAttribute("message", "La URL que intentas visitar no existe.");
        return "error";
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoSuchElementException e, Model model) {
        model.addAttribute("status", 404);
        model.addAttribute("error", "Recurso no encontrado");
        model.addAttribute("message", e.getMessage());
        return "error";
    }


    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDenied(AccessDeniedException e, Model model) {
        model.addAttribute("status", 403);
        model.addAttribute("error", "Acceso Restringido");
        model.addAttribute("message", "No tienes permisos suficientes para realizar esta acción.");
        return "error";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleTypeMismatch(MethodArgumentTypeMismatchException e, Model model) {
        model.addAttribute("status", 400);
        model.addAttribute("error", "Petición Incorrecta");
        String nombreParametro = e.getName();
        String valorInvalido = e.getValue() != null ? e.getValue().toString() : "null";
        model.addAttribute("message",
                String.format("El valor '%s' no es válido para el parámetro '%s'. Se esperaba un número.", valorInvalido, nombreParametro));
        return "error";
    }


    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException e, Model model) {
        model.addAttribute("status", 400);
        model.addAttribute("error", "Solicitud Incorrecta");
        model.addAttribute("message", e.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneralError(Exception e, Model model) {
        // Imprimimos el error en consola para que tú (desarrollador) sepas qué pasó
        e.printStackTrace();

        model.addAttribute("status", 500);
        model.addAttribute("error", "Error Interno del Servidor");
        model.addAttribute("message", "Ocurrió un error inesperado. Por favor intenta más tarde.");
        return "error";
    }
}