import axios from 'axios'

export async function loginUser(params) {
    const { email, contrasena } = params

    return axios.post("http://localhost:8080/users/login", {
        email,
        contrasena
    }, {
        withCredentials: true
    })

}