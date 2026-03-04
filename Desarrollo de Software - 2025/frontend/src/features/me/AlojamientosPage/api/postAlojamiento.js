import axios from "axios";

export async function postAlojamiento(alojamientoData, alojamientoId) {
    const response = await axios.post("http://localhost:8080/alojamientos" + (alojamientoId != null ? `/${alojamientoId}` : ""), alojamientoData, { withCredentials: true })
    return response.data
}