import axios from "axios";

export async function postReserva(reserva) {
    try {
        const response = await axios.post("http://localhost:8080/reservas", reserva, {
        withCredentials: true //mandamos las cookies de sesion para que el back sepa quien hace la reserva
    });
    return response.data; 
    } catch (error) {
        console.error("Error al crear la reserva:", error);
        throw error;
    }
}

