import path from "node:path";
import fs from "node:fs/promises";
import { Notificacion } from "../entities/Notificacion.js";
import mongoose from "mongoose";
import { NotificacionNotFoundError } from "../../exceptions/notificacionNotFound.js";
import { NotificacionModel } from "../schemas/notificacionSchema.js";
export class NotificacionRepository {
  static notificacionesPath = path.join("data", "notificaciones.json");

  async findAll() {
    const notificacion = await NotificacionModel.find();

    return notificacion;
  }

  async findAllByUser(userId) {
    const notificacion = await NotificacionModel.find({
      usuario: userId
    });

    return notificacion;
  }

  async findByPage(pageNum, limitNum, userId) {
    const offset = (pageNum - 1) * limitNum;
    const notificaciones = await NotificacionModel
      .find({
        usuario: userId,
      })
      .sort({ createdAt: 1 })
      .skip(offset)
      .limit(limitNum)
      .exec();
    return notificaciones;
  }

  async findById(id) {
    const notificacion = NotificacionModel.findById(id);
    if (!notificacion) {
      throw new NotificacionNotFoundError(id);
    }
    return notificacion;
  }

  async save(notificacion) {
    if (notificacion.id) {
      await notificacion.save();
    } else {
      const model = NotificacionModel
      const _notificacion = await new model(notificacion).save();
      return _notificacion;
    }
  }

  async update(notificacion) {
    const model = NotificacionModel

    const { id, ...rest } = notificacion;
    const updated = await model
      .findByIdAndUpdate(id, { $set: rest }, { new: true, runValidators: true })
      .exec();

    if (!updated) {
      throw new NotificacionNotFoundError(notificacion.id);
    }
    return updated;
  }

  async countAll() {
    const notificaciones = await this.findAll();
    return notificaciones.length;
  }

  async create(notificationData) {
    const created = new NotificacionModel(notificationData);
    await created.save();
    return created;
  }
}

function mapToNotificaciones(dataObjects) {
  return dataObjects.map(mapToNotificacion);
}

function mapToNotificacion(dataObject) {
  const { mensaje, usuario, reservaAsociada, leida, fechaLeida, fechaAlta } =
    dataObject;
  const notificacion = new Notificacion(mensaje, usuario, reservaAsociada);

  notificacion.id = dataObject.id;
  notificacion.leida = leida || false;
  notificacion.fechaLeida = fechaLeida ? new Date(fechaLeida) : null;
  notificacion.fechaAlta = fechaAlta ? new Date(fechaAlta) : new Date();

  return notificacion;
}

function mapToDataObject(notificacion) {
  //const { mensaje, usuario, reservaAsociada } = notificacion
  return {
    id: notificacion.id,
    mensaje: notificacion.mensaje,
    usuario: notificacion.usuario,
    reservaAsociada: notificacion.reservaAsociada,
    fechaAlta: notificacion.fechaAlta.toISOString(),
    leida: notificacion.leida,
    fechaLeida: notificacion.fechaLeida
      ? notificacion.fechaLeida.toISOString()
      : null,
  };
}

function mapToDataObjects(notificaciones) {
  return notificaciones.map(mapToDataObject);
}
