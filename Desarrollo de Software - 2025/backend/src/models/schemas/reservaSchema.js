import mongoose from 'mongoose'
import { modelNames } from './constants.js'
import { rangoFechasSchema } from './rangoFechasSchema.js'
import { Reserva } from '../entities/Reserva.js'
import { EstadoReserva } from '../enums/EstadoReserva.js'
import { cambioEstadoReservaSchema } from './cambioEstadoReservaSchema.js'
import { fotoSchema } from './fotoSchema.js'
import { direccionSchema } from './direccionSchema.js'
import { Moneda } from "../enums/Moneda.js"
import { Caracteristica } from "../enums/Caracteristica.js"

const reservaSchema = new mongoose.Schema({
    fechaAlta: {
        type: Date,
        required: true,
        default: Date.now
    },
    estado: {
        type: String,
        enum: Object.values(EstadoReserva),
        required: true,
        default: EstadoReserva.PENDIENTE
    },
    huespedReservador: {
        type: mongoose.Schema.Types.ObjectId,
        ref: modelNames.usuario,
        required: true
    },
    alojamiento: {
        type: mongoose.Schema.Types.ObjectId,
        ref: modelNames.alojamiento,
        required: true
    },
    alojamientoSnapshot: {
        nombre: {
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
        fotos: [fotoSchema],
        direccion: direccionSchema,
        anfitrion: {
            type: mongoose.Schema.Types.ObjectId,
            ref: modelNames.usuario,
            required: true
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
        caracteristicas: [{
            type: String,
            required: true,
            enum: Object.values(Caracteristica)
        }]
    },
    rangoFechas: {
        type: rangoFechasSchema,
        required: true
    },
    precioPorNoche: {
        type: Number,
        min: 0,
        required: true
    },
    huespedes: {
        type: Number,
        min: 1,
        required: true
    },
    registroDeCambiosDeEstado: [cambioEstadoReservaSchema]
})

reservaSchema.loadClass(Reserva)

export const ReservaModel = mongoose.model(modelNames.reserva, reservaSchema)