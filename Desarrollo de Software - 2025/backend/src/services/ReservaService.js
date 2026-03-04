import mongoose from "mongoose";
import { AlojamientoNotAvailableError } from "../exceptions/alojamientoNotAvailable.js";
import { RangoFechas } from "../models/entities/RangoFechas.js";
import { Reserva } from "../models/entities/Reserva.js";
import { EstadoReserva } from "../models/enums/EstadoReserva.js";
import { modelNames } from "../models/schemas/constants.js";
import { ReservaIsCancelled } from "../exceptions/reservaExceptions.js";
import { ReservaNotFoundError } from "../exceptions/reservaNotFound.js";
import { CambioEstadoReserva } from "../models/entities/CambioEstadoReserva.js";
import { FactoryNotificacion } from "../models/entities/FactoryNotificacion.js";

export class ReservaService {

    constructor(reservaRepository, alojamientoRepository, notificationRepository) {
        this.reservaRepository = reservaRepository;
        this.alojamientoRepository = alojamientoRepository;
        this.notificationRepository = notificationRepository;
    }

    async findById(id, userId) {
        const reserva = await this.reservaRepository.findById(id);
        if (reserva == null || !reserva.huespedReservador.equals(userId)) {
            throw new ReservaNotFoundError(id)
        }
        return this.toDTO(reserva);
    }


    async create(huesped, alojamiento, rangoFechas, huespedes) {
        const _alojamiento = await this.alojamientoRepository.findByIdPopulateReservas(alojamiento)

        if (huespedes > _alojamiento.cantHuespedesMax) {
            throw new Error('cantidad de huespedes excedida')
        }

        const _rangoFechas = new RangoFechas(new Date(rangoFechas.desde), new Date(rangoFechas.hasta))
        const disponible = _alojamiento.estasDisponibleEn(_rangoFechas);
        if (!disponible) { throw new AlojamientoNotAvailableError(_alojamiento) }; //error se arroja aca porque se relaciona con nuestra logica de negocio

        const reserva = new Reserva(huesped, _alojamiento, _rangoFechas, huespedes)

        reserva.actualizarEstado(new CambioEstadoReserva(new Date(), EstadoReserva.PENDIENTE, huesped))
        const nuevaReserva = await this.reservaRepository.save(reserva)
        const newNotification = FactoryNotificacion.crearSegunReserva(nuevaReserva)
        this.notificationRepository.save(newNotification)

        return this.toDTO(nuevaReserva)
    }

    async delete(id, huespedId) {
        const reserva = await this.reservaRepository.findById(id);

        if (!reserva) {
            return 'reserva_inexistente';
        }

        const inicio = new Date(reserva.rangoFechas.fechaInicio).getTime();
        if (inicio <= Date.now()) {
            return 'reserva_iniciada';
        }

        const cambioEstado = new CambioEstadoReserva(new Date(), EstadoReserva.CANCELADA, huespedId)
        // Cambio de estado en lugar de borrado
        reserva.actualizarEstado(cambioEstado)

        await this.reservaRepository.save(reserva);

        const newNotification = FactoryNotificacion.crearSegunReserva(reserva)
        this.notificationRepository.save(newNotification)

        return true;
    }

    async confirmarReserva(reservaId, userId) {
        const reserva = await this.reservaRepository.findByIdPopulatedAlojamiento(reservaId)
        if (!reserva.tieneAnfitrionA(userId) || reserva.estado === EstadoReserva.CANCELADA) {
            throw new ReservaNotFoundError()
        }
        reserva.actualizarEstado(new CambioEstadoReserva(new Date(), EstadoReserva.CONFIRMADA, userId))
        await this.reservaRepository.save(reserva)
        const newNotification = FactoryNotificacion.crearSegunReserva(reserva)
        this.notificationRepository.save(newNotification)
        return this.toDTO(reserva)
    }


    async findAll({ user = null, page = 1, limit = 10 }) {
        // 1) Normalizar page y limit a números y asegurar al menos 1
        const pageNum = Math.max(Number(page), 1);
        const limitNum = Math.max(Number(limit), 1);

        // 2) Traer la página correcta
        const allReservas = await this.reservaRepository.findByUser(user);
        let pageReservas = allReservas.slice(
            (pageNum - 1) * limitNum,
            (pageNum - 1) * limitNum + limitNum
        );

        // 4) Calcular totales
        const total = allReservas.length;
        const total_pages = Math.ceil(total / limitNum);

        // 5) Devolver el DTO paginado
        return {
            page: pageNum,
            per_page: limitNum,
            total: total,
            total_pages: total_pages,
            data: pageReservas.map(r => this.toDTO(r))
        };
    }


    async update(id, nuevasFechas, userId) {
        const reserva = await this.reservaRepository.findByIdPopulatedAlojamiento(id);
        if (!reserva || !reserva.huespedReservador.equals(userId)) { throw new ReservaNotFoundError(id) };
        if (reserva.estado === EstadoReserva.CANCELADA) {

            throw new ReservaIsCancelled(id);

        }
        const _nuevasFechas = new RangoFechas(new Date(nuevasFechas.desde), new Date(nuevasFechas.hasta))
        const alojamiento = reserva.alojamiento;
        const disponible = alojamiento.reservas.every(r => r.id === reserva.id || r.reservaValida(_nuevasFechas));
        //                       esto da true e ignora la reserva con mismo id  ||   aca despues se fija las demas (las que el id no es igual)
        if (!disponible) { throw new AlojamientoNotAvailableError(alojamiento) };

        reserva.rangoFechas = _nuevasFechas;
        reserva.actualizarEstado(new CambioEstadoReserva(new Date(), EstadoReserva.PENDIENTE, userId))
        await this.reservaRepository.save(reserva)
        const newNotification = FactoryNotificacion.crearSegunReserva(reserva)
        this.notificationRepository.save(newNotification)
        return this.toDTO(reserva);
    }

    async findByAnfitrion(userID) {
        const reservas = []
        const alojamientos = await this.alojamientoRepository.findByAnfitrion(userID)
        alojamientos.forEach(el => reservas.push(...el.reservas))
        return reservas
    }


    toDTO(reserva) {
        return {
            id: reserva.id,
            huespedes: reserva.huespedes,
            huesped: reserva.huespedReservador,
            alojamiento: reserva.alojamiento,
            alojamientoSnapshot: reserva.alojamientoSnapshot,
            rangoFechas: reserva.rangoFechas,
            estado: reserva.estado
        }
    };

}

/*
    Otra opcion

    update(id, nuevasFechas){
        const reserva = this.reservaRepository.findById(id);
        if (!reserva){
            return null;
        }

        if (reserva.estado === EstadoReserva.CANCELADA){
            return false;
        }

        const alojamiento = reserva.alojamiento;
        alojamiento.reservas = alojamiento.reservas.filter(r => r.id !== reserva.id);

        const disponible = alojamiento.estasDisponibleEn(nuevoRangoFechas);
        if(!disponible){
        alojamiento.reservas.push(reserva);
        return false;
        }

        reserva.rangoFechas = nuevoRangoFechas;
        alojamiento.reservas.push(reserva);
        
        this.reservaRepository.update(reserva);
        return this.toDTO(reserva);
    }
*/