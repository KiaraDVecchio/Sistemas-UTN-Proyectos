import axios from "axios";

export async function getMyAlojamientos() {
    const response = await axios.get("http://localhost:8080/alojamientos/me", { withCredentials: true })
    return response.data
}