import mongoose from "mongoose"
import { modelNames } from "./constants.js"
import { Ciudad } from "../entities/Ciudad.js"

const ciudadSchema = new mongoose.Schema({
    nombre: { type: String, required: true, trim: true },
    pais: {
        type: mongoose.Schema.Types.ObjectId,
        ref: modelNames.pais,
        required: true
    }
})

ciudadSchema.loadClass(Ciudad)

export const CiudadModel = mongoose.model(modelNames.ciudad, ciudadSchema)