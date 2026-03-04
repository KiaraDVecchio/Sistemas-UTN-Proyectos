import axios from "axios";

export async function deleteReservaById(id) {
    try {
        const response = await axios.delete(`http://localhost:8080/reservas/${id}`, {
            withCredentials: true,
        });
        return response.status
    } catch (error) {
        console.error("Error en la llamada a la api")
        throw error;
    }
}

