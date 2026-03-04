import { ReservaNotFoundError } from "../../exceptions/reservaNotFound.js";
import { EstadoReserva } from "../enums/EstadoReserva.js";
import { cambioEstadoReservaSchema } from "../schemas/cambioEstadoReservaSchema.js";
import { CambioEstadoReserva } from "./CambioEstadoReserva.js";
import { FactoryNotificacion } from "./FactoryNotificacion.js";
import { RangoFechas } from "./RangoFechas.js";

export class Reserva {
    // #id;

    constructor(huespedReservador, alojamiento, rangoFechas, guests) {
        this.huespedes = guests;
        this.fechaAlta = new Date();
        this.estado = EstadoReserva.PENDIENTE;
        this.huespedReservador = huespedReservador;
        this.alojamiento = alojamiento;
        this.alojamientoSnapshot = {
            horarioCheckOut: alojamiento.horarioCheckOut,
            direccion: alojamiento.direccion,
            horarioCheckIn: alojamiento.horarioCheckIn,
            moneda: alojamiento.moneda,
            precioPorNoche: alojamiento.precioPorNoche,
            nombre: alojamiento.nombre,
            anfitrion: alojamiento.anfitrion,
            caracteristicas: alojamiento.caracteristicas,
            fotos: alojamiento.fotos
        };
        this.rangoFechas = rangoFechas;
        this.precioPorNoche = alojamiento.getPrecioPorNoche();
        this.registroDeCambiosDeEstado = [];
    }

    actualizarEstado(estadoReserva) {
        this.estado = estadoReserva.estado;
        this.registroDeCambiosDeEstado.push(estadoReserva);
    }

    tieneAnfitrionA(anfitrionId) {
        return this.alojamiento.esDeAnfitrion(anfitrionId)
    }

    reservaValida(nuevoRango) {
        return !(
            (this.estado === EstadoReserva.CONFIRMADA || this.estado === EstadoReserva.PENDIENTE) &&
            new RangoFechas(this.rangoFechas.fechaInicio, this.rangoFechas.fechaFin).seSuperponeCon(nuevoRango)
        );
    }
}
