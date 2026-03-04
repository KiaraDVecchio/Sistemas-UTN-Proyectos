import { Notificacion } from "../models/entities/Notificacion.js";

export class NotificacionService {

    constructor(notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    async findAll({ usuarioId = null, leidas = null }) {
        let lista = await this.notificacionRepository.findAllByUser(usuarioId);

        // 3) Filtrar por leídas/no leídas, si lo pasaron
        if (leidas != null) {
            lista = lista.filter(n => n.leida === leidas);
        }

        // 6) DTO
        const data = lista.map(n => this.toDTO(n));

        return {
            data
        };
    }

    async findById(id) {
        const notificacion = await this.notificacionRepository.findById(id);
        return notificacion ? this.toDTO(notificacion) : null;
    }

    async update(id) {
        const notificacion = await this.notificacionRepository.findById(id);

        if (!notificacion) return { error: "not-found" };

        if (notificacion.leida) return { error: "La notificacion ya fue leida" };

        notificacion.marcarComoLeida();

        const actualizado = await this.notificacionRepository.update(notificacion);
        return this.toDTO(actualizado);

    }

    toDTO(notificacion) {
        return {
            id: notificacion.id,
            mensaje: notificacion.mensaje,
            usuario: notificacion.usuario,
            reservaAsociada: notificacion.reservaAsociada,
            fechaAlta: notificacion.fechaAlta,
            leida: notificacion.leida,
            fechaLeida: notificacion.fechaLeida,
        }
    }

    async create(dto) {
        if (!dto.mensaje || !dto.usuario) {
            throw new Error("Mensaje y usuario son obligatorios.");
        }
        const nuevaNotificacion = await this.notificacionRepository.create(dto);
        return nuevaNotificacion;
    }

}