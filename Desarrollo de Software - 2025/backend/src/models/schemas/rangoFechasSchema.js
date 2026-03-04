import mongoose from "mongoose";

export const rangoFechasSchema = new mongoose.Schema({
    fechaInicio: { type: Date, required: true },
    fechaFin: { type: Date, required: true }
}, { _id: false })