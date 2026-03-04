import axios from "axios";

export async function getNotifications() {
    const response = await axios.get("http://localhost:8080/notificaciones", { withCredentials: true })
    return response.data
}