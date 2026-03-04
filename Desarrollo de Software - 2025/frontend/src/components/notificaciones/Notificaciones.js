import "./Notificaciones.css"
import Dropdown from 'react-bootstrap/Dropdown';
import icon from "./icon/icons8-campana-32.png"
// import { notificaciones as notificacionesMock } from "../../mockData/notificaciones"; 
import { useEffect, useMemo, useState } from "react";
import { getNotifications } from "./api/getNotifications";
import { readNotification } from "./api/readNotification";
import { useNavigate } from "react-router-dom";

export function Notificaciones() {
  const [notificaciones, setNotificaciones] = useState([]);

  useEffect(() => {
    getNotifications().then(res => {
      setNotificaciones(res.data)
    })
  }, [])

  function updateNotification(notification) {
    setNotificaciones(prev => {
      const newPrev = JSON.parse(JSON.stringify(notificaciones))
      const index = newPrev.findIndex(el => el.id === notification.id)
      if (index != -1) {
        newPrev.splice(index, 1, notification)
      }
      return newPrev
    })
  }

  const notificacionesNoLeídas = useMemo(() => notificaciones.filter(el => el.leida === false), [notificaciones])
  const notificacionesLeidas = useMemo(() => notificaciones.filter(el => el.leida === true), [notificaciones])

  const allNotificaciones = useMemo(() => [...notificacionesNoLeídas, ...notificacionesLeidas], [notificacionesNoLeídas, notificacionesLeidas])
  return (
    <Dropdown>
      <Dropdown.Toggle variant="none" id="dropdown-basic" className="notificacion-toggle">
        <img
          src={icon}
          alt="Notificaciones"
          className="icono-campana"
        >
        </img>
        {notificacionesNoLeídas.length > 0 && (
          <span className="badge">{notificacionesNoLeídas.length}</span>
        )}
      </Dropdown.Toggle>

      <Dropdown.Menu style={{ maxHeight: '250px', overflowY: 'auto', overflowX: "hidden" }}>
        {allNotificaciones.length > 0 ? (
          allNotificaciones.map((n) => (
            <NotificacionItem key={n.id} data={n} updateNotification={updateNotification} />
          ))
        ) : (
          <Dropdown.Item disabled> Por ahora, no hay nada aquí.</Dropdown.Item>
        )}
      </Dropdown.Menu>
    </Dropdown>
  );
}

const NotificacionItem = ({ data, updateNotification }) => {

  const navigate = useNavigate()

  const marcarComoLeida = (id) => {
    readNotification(id).then(res => {
      updateNotification(res)
    })
  };

  return <Dropdown.Item key={data.id} onClick={() => {
    if (data.leida === false) {
      marcarComoLeida(data.id)
    }
    if (data.mensaje === "Reserva confirmada") {
      navigate("/me/reservas")
    } else {
      navigate("/me/admin/dashboard")
    }
  }}>
    <span style={{ fontWeight: data.leida === false ? 600 : 400 }}>
      {data.mensaje}
    </span>
  </Dropdown.Item>
}