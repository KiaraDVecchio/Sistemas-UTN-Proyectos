import axios from "axios";

export async function readNotification(id) {
    const response = await axios.patch("http://localhost:8080/notificaciones/" + id, "", { withCredentials: true })
    return response.data
} 