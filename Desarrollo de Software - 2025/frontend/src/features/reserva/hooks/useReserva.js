import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getReservaById } from "../api/getReservaById";
import { getAnfitrionById } from "../api/getAnfitrionById";

export function useReserva() {
  const { id } = useParams();
  const [state, setState] = useState({ loading: true, error: false, data: undefined });

  useEffect(() => {
    setState({ loading: true, error: false, data: undefined });

    getReservaById(id)
      .then(reserva => {
        getAnfitrionById(reserva.alojamientoSnapshot.anfitrion).then(res => {
          setState({ loading: false, error: false, data: reserva, anfitrion: res });
        })
      })
      .catch(err => {
        console.error(err);
        setState({ loading: false, error: true, data: undefined });
      });
  }, [id]);

  return state;
}