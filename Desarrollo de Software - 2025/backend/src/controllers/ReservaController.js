import { TipoUsuario } from "../models/enums/TipoUsuario.js";
import { Usuario } from "../models/entities/Usuario.js";
import { Alojamiento } from "../models/entities/Alojamiento.js";
import { RangoFechas } from "../models/entities/RangoFechas.js";


export class ReservaController {

    constructor(reservaService) {
        this.reservaService = reservaService;
    }

    async findAll(req, res) {
        const user = req.user.id
        const { page = 1, limit = 10 } = req.query;
        const reservasPaginadas = await this.reservaService.findAll({ user, page, limit });
        res.json(reservasPaginadas);
    };

    async findById(req, res) {
        const userId = req.user.id
        const id = req.params.id;
        const reserva = await this.reservaService.findById(id, userId);
        //se borra el if para manejarlo a nivel de middleware 
        res.json(reserva);
    };

    async findByAnfitrion(req, res) {
        const userId = req.user.id
        const reservas = await this.reservaService.findByAnfitrion(userId)
        res.json(reservas)
    }

    async create(req, res, next) {
        const { alojamiento, rangoFechas, huespedes } = req.body;

        const guests = Number(huespedes)

        if (Number.isNaN(guests)) {
            return res.status(400).json({ error: "Huéspedes inválidos" });
        }

        if (!alojamiento || !rangoFechas) {
            return res.status(400).json({ error: "Datos inválidos" });
        }

        if (!rangoFechas.desde || !rangoFechas.hasta || new Date(rangoFechas.desde) >= new Date(rangoFechas.hasta)) {
            return res.status(400).json({ error: "La fecha de inicio debe ser anterior a la de fecha de fin" });
        }

        const nuevaReserva = await this.reservaService.create(req.user.id, alojamiento, rangoFechas, guests)

        res.status(200).json(nuevaReserva); //si salio todo bien
    }

    async delete(req, res) { //agregar middlewares
        const id = req.params.id;
        const eliminado = await this.reservaService.delete(id, req.user.id);
        if (eliminado === 'reserva_inexistente')
            return res.status(404).json({ res: 'err', msg: "Reserva no encontrada" });
        if (eliminado === 'reserva_iniciada')
            return res.status(403).json({ res: 'err', msg: "Operación no disponible" });
        res.status(204).send();
    };

    async confirm(req, res) { //agregar middlewares
        const id = req.params.id;
        const reserva = await this.reservaService.confirmarReserva(id, req.user.id);
        res.status(204).send(reserva);
    };

    async update(req, res) {
        const id = req.params.id;
        const rangoFechas = req.body;

        if (!rangoFechas) {
            return res.status(400).json({ error: "Datos inválidos" });
        }

        if (!rangoFechas.desde || !rangoFechas.hasta || new Date(rangoFechas.desde) >= new Date(rangoFechas.hasta)) {
            return res.status(400).json({ error: "La fecha de inicio debe ser anterior a la de fecha de fin" });
        }

        const modificacion = await this.reservaService.update(id, rangoFechas, req.user.id);

        if (modificacion === false) {
            return res.status(400).json({ error: "No se puede modificar la reserva" });
        }

        return res.status(200).json(modificacion);

    }

}