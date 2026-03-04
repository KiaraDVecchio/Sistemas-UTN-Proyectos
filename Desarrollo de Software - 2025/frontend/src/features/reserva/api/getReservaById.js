import axios from "axios";

export async function getReservaById(id) {
    try {
        const response = await axios.get(`http://localhost:8080/reservas/${id}`, {
            withCredentials: true,
        });
        return response.data; // devuelve solo los datos de la reserva
    } catch (error) {
        console.error("Error en la llamada a la api")
        throw error;
    }
}

