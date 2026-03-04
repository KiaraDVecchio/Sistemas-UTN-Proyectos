import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getAlojamientoById } from "../api/getAlojamientoById";

export function useAlojamiento() {
    const [state, setState] = useState({ loading: true, error: false, data: undefined })

    const { id } = useParams();


    useEffect(() => {
        setState({ loading: true, error: false, data: undefined })
        getAlojamientoById(id).then(res => {
            if (res.status === 200) {
                setState({ loading: false, error: false, data: res.data })
            } else {
                setState({ loading: false, error: true, data: undefined })
            }
        }).catch(err => {
            console.error(err)
            setState({ loading: false, error: true, data: undefined })
        })
    }, [id])

    return state
}