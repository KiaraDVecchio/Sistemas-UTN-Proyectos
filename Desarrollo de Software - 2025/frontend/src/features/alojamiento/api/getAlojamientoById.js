import axios from "axios";

export async function getAlojamientoById(id) {
    return axios.get(`http://localhost:8080/alojamientos/${id}`)
}