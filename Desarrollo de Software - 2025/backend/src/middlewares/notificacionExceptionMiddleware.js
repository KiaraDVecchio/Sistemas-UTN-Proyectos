// src/middlewares/notificacionExceptionMiddleware.js
import { NotificacionNotFoundError } from "../exceptions/notificacionNotFound.js";


/**
 * Middleware global de manejo de errores para rutas de notificación.
 * Detecta errores específicos y responde con el status y mensaje apropiado.
 */
export function notificacionExceptionMiddleware(err, req, res, next) {
  // Error de notificación no encontrada
  if (err instanceof NotificacionNotFoundError) {
    return res.status(404).json({
      error: "Notificacion not found",
      id: err.id,
      message: err.message
    });
  }

  // Errores de validación de Mongoose
  if (err.name === "ValidationError") {
    return res.status(400).json({
      error: "Validation Error",
      details: err.errors
    });
  }

  // Otros errores:
  console.error(err);
  res.status(err.status || 500).json({
    error: err.message || "Internal Server Error"
  });
}
