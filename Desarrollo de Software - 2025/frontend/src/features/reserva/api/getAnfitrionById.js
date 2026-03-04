import axios from "axios";

export async function getAnfitrionById(id) {
    try {
        const response = await axios.get(`http://localhost:8080/users/anfitrion/${id}`);
        return response.data; // devuelve solo los datos del anfitrion
    } catch (error) {
        console.error("Error en la llamada a la api")
        throw error;
    }
}