import mongoose from "mongoose";
import { EstadoReserva } from "../enums/EstadoReserva.js";
import { modelNames } from "./constants.js";

export const cambioEstadoReservaSchema = new mongoose.Schema({
    fecha: {
        type: Date,
        default: Date.now
    },
    estado: { type: String, enum: Object.values(EstadoReserva), required: true },
    usuario: {
        type: mongoose.Schema.Types.ObjectId,
        ref: modelNames.usuario,
        required: true
    }
})
