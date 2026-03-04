import axios from 'axios'

const TIPOS_USUARIO = ["ANFITRION", "HUESPED"]

export async function registerUser(params) {
    const {
        email, contrasena, confirmarContrasena, nombre, tipo = "HUESPED"
    } = params

    if (!TIPOS_USUARIO.includes(tipo)) {
        throw new Error("Tipo de usuario no válido")
    } else {
        return axios.post("http://localhost:8080/users", {
            email,
            contrasena,
            confirmarContrasena,
            nombre, tipo
        }, {
            withCredentials: true
        })
    }
}