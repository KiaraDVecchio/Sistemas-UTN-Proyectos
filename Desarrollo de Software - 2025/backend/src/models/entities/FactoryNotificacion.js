import { EstadoReserva } from "../enums/EstadoReserva.js";
import { Notificacion } from "./Notificacion.js";

export class FactoryNotificacion {
    static crearSegunReserva(reserva) {
        if (reserva.estado === EstadoReserva.PENDIENTE) {
            return new Notificacion("Nueva solicitud de reserva", reserva.alojamientoSnapshot.anfitrion, reserva.id);
        }
        if (reserva.estado === EstadoReserva.CONFIRMADA) {
            return new Notificacion("Reserva confirmada", reserva.huespedReservador, reserva.id);
        }
        if (reserva.estado === EstadoReserva.CANCELADA) {
            return new Notificacion("Reserva cancelada", reserva.alojamientoSnapshot.anfitrion, reserva.id);
        }
    }
}

// Si es una factory solo por nombre lo dejaría así. Si hay que implementar el
// patrón de diseño de Factory Method, habría que crear las subclases que heredan de notification,
// pero no está representado en el diagrama de clases propuesto