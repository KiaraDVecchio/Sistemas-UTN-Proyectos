import { AlojamientoNotFoundException } from "../exceptions/alojamientoNotFoundException.js";
import { BadRequestException } from "../exceptions/badRequestException.js";

export function alojamientoExceptionMiddleware(err, req, res, next) {
  if (err.constructor.name == AlojamientoNotFoundException.name) {
    return res.status(404).json({ error: err.message });
  }
  
  if (err.constructor.name == BadRequestException.name) {
    return res.status(err.status).json({ error: err.message });
  }
  return res.status(500).json({ error: err.stack });
}
