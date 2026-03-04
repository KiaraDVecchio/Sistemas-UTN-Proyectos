import axios from "axios";

export async function getReservas(page = 1) {
    try {
        const response = await axios.get(`http://localhost:8080/reservas?page=${page}`, {
            withCredentials: true,
        })
        return response.data;
    } catch (error) {
        console.error("Error en la llamada a la api")
        throw error;
    }
}
