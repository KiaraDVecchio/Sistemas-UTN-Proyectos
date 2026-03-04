import axios from "axios";

//corregir
export async function loginUser(credentials) {
  // credentials = { email: string, contrasena: string }
    return axios.post("http://localhost:8080/users/login", credentials, {
    withCredentials: true 
});
}