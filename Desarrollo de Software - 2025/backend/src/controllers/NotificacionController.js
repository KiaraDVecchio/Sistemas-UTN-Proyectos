import { NotificacionNotFoundError } from "../exceptions/notificacionNotFound.js";
import { Notificacion } from "../models/entities/Notificacion.js";

export class NotificacionController {
  constructor(notificacionService) {
    this.notificacionService = notificacionService;
  }

  async findAll(req, res, next) {
    try {
      const usuarioId = req.user.id
      let leidasParam = null;
      if (req.query.leidas === "true") leidasParam = true;

      const notificaciones = await this.notificacionService.findAll({
        usuarioId,
        leidas: leidasParam
      });

      res.json(notificaciones);
    } catch (error) {
      next(error);
    }
  }

  async findById(req, res, next) {
    try {
      const id = req.params.id;
      const notificacion = await this.notificacionService.findById(id);
      if (!notificacion) {
        throw new NotificacionNotFoundError();
      }
      res.json(notificacion);
    } catch (error) {
      next(error);
    }
  }

  async update(req, res, next) {
    try {
      const id = req.params.id;
      const actualizado = await this.notificacionService.update(id);
      if (!actualizado) {
        throw new NotificacionNotFoundError();
      }
      res.json(actualizado);
    } catch (error) {
      next(error);
    }
  }

  async create(req, res, next) {
    try {
      const dto = {
        mensaje: req.body.mensaje,
        usuario: req.body.usuario,
        reservaAsociada: req.body.reservaAsociada
      };
      const notificacion = await this.notificacionService.create(dto);
      res.status(201).json(notificacion);
    } catch (err) {
      next(err);
    }
  }

}
