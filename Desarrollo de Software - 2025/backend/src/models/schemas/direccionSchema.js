import mongoose from "mongoose";
import { modelNames } from "./constants.js";

export const direccionSchema = new mongoose.Schema({
    calle: { type: String, required: true },
    altura: { type: String, required: true },
    ciudad: {
        type: mongoose.Schema.Types.ObjectId,
        ref: modelNames.ciudad
    },
    lat: { type: Number },
    long: { type: Number }
}, { _id: false })