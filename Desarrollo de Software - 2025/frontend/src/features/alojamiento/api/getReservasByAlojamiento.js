import axios from "axios";

export async function getReservasByAlojamiento(descripcion) {
    try{
    const response = await axios.get(`http://localhost:8080/reservas`, {
        withCredentials: true,
    });
    const reservas = response.data;
    return reservas.filter(r => r.descripcion === descripcion);
    
} catch(error){
    console.error("error en la llamada a la api")
    throw error;
}
}
