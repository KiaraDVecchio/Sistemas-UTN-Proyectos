import mongoose from "mongoose"

import { Moneda } from "../enums/Moneda.js"
import { Alojamiento } from "../entities/Alojamiento.js"
import { Caracteristica } from "../enums/Caracteristica.js"
import { modelNames } from "./constants.js"
import { direccionSchema } from "./direccionSchema.js"
import { fotoSchema } from "./fotoSchema.js"

export const alojamientoSchema = new mongoose.Schema({
    anfitrion: {
        type: mongoose.Schema.Types.ObjectId,
        ref: modelNames.usuario,
        required: true
    },
    nombre: {
        type: String,
        required: true,
        trim: true
    },
    descripcion: {
        type: String,
        required: true,
        trim: true
    },
    precioPorNoche: {
        type: Number,
        required: true,
        min: 0
    },
    moneda: {
        type: String,
        required: true,
        enum: Object.values(Moneda)
    },
    horarioCheckIn: {
        type: String,
        required: true,
        validate: {
            validator: function (value) {
                return /^([01]\d|2[0-3]):[0-5]\d$/.test(value)
            },
            message: props => `${props.value} no es una hora váida (hh:mm)`
        }
    },
    horarioCheckOut: {
        type: String,
        required: true,
        validate: {
            validator: function (value) {
                return /^([01]\d|2[0-3]):[0-5]\d$/.test(value)
            },
            message: props => `${props.value} no es una hora váida (hh:mm)`

        }
    },
    cantHuespedesMax: {
        type: Number,
        min: 1,
        required: true
    },
    caracteristicas: [{
        type: String,
        required: true,
        enum: Object.values(Caracteristica)
    }],
    direccion: direccionSchema,
    fotos: [fotoSchema]
}, { timestamps: true })

alojamientoSchema.virtual("reservas", {
    ref: modelNames.reserva,
    localField: "_id",
    foreignField: "alojamiento",
});

alojamientoSchema.set("toObject", { virtuals: true });
alojamientoSchema.set("toJSON", { virtuals: true });

alojamientoSchema.loadClass(Alojamiento)

export const AlojamientoModel = mongoose.model(modelNames.alojamiento, alojamientoSchema)
