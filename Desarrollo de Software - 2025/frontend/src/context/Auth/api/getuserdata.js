import axios from "axios";

export function getuserdata({ signal }) {
    return axios.get('http://localhost:8080/users', {
        withCredentials: true,
        signal
    })
}