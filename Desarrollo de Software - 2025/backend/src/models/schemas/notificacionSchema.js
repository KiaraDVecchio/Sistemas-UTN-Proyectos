import mongoose from "mongoose";
import { string } from "zod";
import { modelNames } from "./constants.js";
import { Notificacion } from "../entities/Notificacion.js";

const { Schema, model } = mongoose;

// Definicion del esquema de Notificacion acorde a la entidad
const notificacionSchema = new Schema(
  {
    usuario: {
      type: Schema.Types.ObjectId,
      ref: "Usuario",
      required: true,
    },
    reservaAsociada: {
      type: Schema.Types.ObjectId,
      ref: "Reserva",
      required: false,
    },
    mensaje: {
      type: String,
      required: true,
      trim: true,
    },
    fechaAlta: {
      type: Date,
      default: Date.now,
    },
    leida: {
      type: Boolean,
      default: false,
    },
    fechaLeida: {
      type: Date,
      default: null,
    },
  }
);

notificacionSchema.methods.marcarComoLeida = function () {
  if (!this.leida) {
    this.fechaLeida = new Date();
    this.leida = true;
  } else {
    throw new Error("La notificacion ya fue leida");
  }
};


notificacionSchema.set("toJSON", {
  virtuals: true,
  versionKey: false,
  transform: (_doc, ret) => {
    ret.id = ret._id;
    delete ret._id;
  },
});

export const NotificacionModel = mongoose.model(modelNames.notificacion, notificacionSchema)

export { notificacionSchema };

