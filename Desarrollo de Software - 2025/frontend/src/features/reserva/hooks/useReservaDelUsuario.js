import { useEffect, useState, setState } from "react";
import { getReservas } from "../api/getReservas";

export function useReservaDelUsuario(page) {
  const [state, setState] = useState({ loading: true, error: false, data: null })


  function cancelarReserva(reservaID) {
    setState(prev => {
      const newPrev = JSON.parse(JSON.stringify(prev));
      const reservasArray = newPrev.data?.data;

      if (Array.isArray(reservasArray)) {
        const reserva = reservasArray.find(el => el.id === reservaID);
        if (reserva) {
          reserva.estado = "CANCELADA";
        }
      }

      return newPrev;
    });
  }

  function updateReserva(reserva) {
    setState(prev => {
      const newPrev = JSON.parse(JSON.stringify(prev));
      const reservasArray = newPrev.data?.data;

      if (Array.isArray(reservasArray)) {
        const _reserva = reservasArray.find(el => el.id === reserva.id);
        if (_reserva) {
          _reserva.estado = "PENDIENTE";
          _reserva.rangoFechas = reserva.rangoFechas
        }
      }

      return newPrev;
    });
  }


  const actions = { cancelarReserva, updateReserva }

  useEffect(() => {
    setState({ loading: true, error: false, data: [] });

    getReservas(page)
      .then(reservas => {
        setState({ loading: false, error: false, data: reservas });
      })
      .catch(err => {
        console.error(err);
        setState({ loading: false, error: true, data: [] });
      });
  }, [page]);

  return { state, actions };
}