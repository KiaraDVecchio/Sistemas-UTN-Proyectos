import mongoose from "mongoose"
import { modelNames } from "./constants.js"
import { TipoUsuario } from "../enums/TipoUsuario.js"
import { Usuario } from "../entities/Usuario.js"

const usuarioSchema = new mongoose.Schema({
    nombre: {
        type: String,
        required: true,
        trim: true
    },
    email: {
        type: String,
        required: true,
        trim: true,
        validate: {
            validator: function (val) {
                return /^[a-zA-Z0-9_.±]+@[a-zA-Z0-9-]+.[a-zA-Z0-9-.]+$/.test(val)
            },
            message: (props) => `"${props.value}" no es una dirección de correo electronica válida`
        }
    },
    contrasena: {
        type: String,
        required: true
    },
    tipo: {
        type: String,
        required: true,
        enum: Object.values(TipoUsuario)
    }
}, { timestamps: true })

usuarioSchema.loadClass(Usuario)

export const UsuarioModel = mongoose.model(modelNames.usuario, usuarioSchema)
