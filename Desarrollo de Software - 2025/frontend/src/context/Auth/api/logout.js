import axios from 'axios'

export async function logoutUser() {
    return axios.post("http://localhost:8080/users/logout", {}, {
        withCredentials: true
    })
}