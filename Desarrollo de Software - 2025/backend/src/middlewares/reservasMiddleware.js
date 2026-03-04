import { AlojamientoNotAvailableError } from "../exceptions/alojamientoNotAvailable.js";
import { ReservaNotFoundError } from "../exceptions/reservaNotFound.js";

export function reservasErrorHandler(err, req, res, next) {


    if (err.constructor.name == ReservaNotFoundError.name) {
        res.status(404).json({ res: 'err', code: 'NOT_FOUND', msg: `Booking with id ${err.reservaId} not found` })
        return
    }

    if (err.constructor.name == AlojamientoNotAvailableError.name) {
        res.status(400).json({ error: err.message })
        return
    }

    return res.status(500).json({ error: err.stack }) //debatir si es correcto mostrar stack o no
}

