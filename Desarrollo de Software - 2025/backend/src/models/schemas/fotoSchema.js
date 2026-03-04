import mongoose from "mongoose";

export const fotoSchema = new mongoose.Schema({
    path: {
        type: String,
        required: true,
        trim: true
    },
    description: {
        type: String
    }
}, { _id: false })