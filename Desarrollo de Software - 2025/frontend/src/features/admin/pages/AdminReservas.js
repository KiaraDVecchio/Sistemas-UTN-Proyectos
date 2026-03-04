import axios from "axios"
import { useEffect, useMemo, useState } from "react"
import { useFetchState } from "../../../hooks/useFetchState"
import { Loader } from "../../../components/shared/Loader"
import { Button, Modal, ModalBody, Spinner } from "react-bootstrap"
import dayjs from "dayjs"
import { useOutletContext } from "react-router-dom"

export const AdminReservasPage = () => {

    const { reservasState: state, reservasActions: actions, controllers: { getReservas } } = useOutletContext()

    const [openReserva, setOpenReserva] = useState(null)

    function confirmReserva(reservaId) {
        const newState = JSON.parse(JSON.stringify(state.data ?? []))
        const reservaObject = newState?.find(reserva => reserva._id === reservaId)
        if (reservaObject != null) {
            reservaObject.estado = "CONFIRMADA"
        }
        actions.receiveData(newState)
    }

    const openReservaData = useMemo(() => state.data?.find(reserva => reserva._id === openReserva), [openReserva, state.data])

    return (
        <div>
            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "32px" }}>
                <h1>Administrador de reservas</h1>
            </div>
            {state.loading && <Loader />}
            {state.error && <div>
                Ups! Ocurrió un error...
                <Button onClick={getReservas}>Intente nuevamente</Button>
            </div>}
            {!state.error && !state.loading && state.data != null && (
                <table style={{ verticalAlign: "middle" }} className="table">
                    <thead>
                        <tr>
                            <th className="col">Alojamiento</th>
                            <th className="col">Precio</th>
                            <th className="col">Día entrada</th>
                            <th className="col">Día salida</th>
                            <th className="col">Precio total</th>
                            <th className="col">Estado</th>
                            <th className="col">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        {state.data.map(el => (
                            <tr key={el._id}>
                                <td>{el.alojamientoSnapshot.nombre}</td>
                                <td>{el.alojamientoSnapshot.precioPorNoche} {el.alojamientoSnapshot.moneda}</td>
                                <td>{dayjs(el.rangoFechas.fechaInicio).format("DD/MM/YYYY")}</td>
                                <td>{dayjs(el.rangoFechas.fechaFin).format("DD/MM/YYYY")}</td>
                                <td>{dayjs(el.rangoFechas.fechaFin).diff(dayjs(el.rangoFechas.fechaInicio), "days") * el.alojamientoSnapshot.precioPorNoche} {el.alojamientoSnapshot.moneda}</td>
                                <td>{el.estado}</td>
                                <td>
                                    <Button onClick={() => { setOpenReserva(el._id) }} variant="link" className="me-1">
                                        Ver cambios de estado
                                    </Button>
                                    {el.estado === "PENDIENTE" && (
                                        <ConfirmButton reservaId={el._id} confirmStatusReserva={confirmReserva} />
                                    )}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
            {openReservaData != null && <Modal show={openReserva} onHide={() => { setOpenReserva(null) }} centered>
                <Modal.Header>
                    <span className="fw-bold">{openReservaData.alojamientoSnapshot.nombre}</span>
                    <Button onClick={() => {
                        getReservas()
                    }}>Actualizar</Button>
                    {/* <span>{dayjs(openReservaData.rangoFechas.fechaInicio).format("DD/MM/YYYY")} - {dayjs(openReservaData.rangoFechas.fechaFin).format("DD/MM/YYYY")}</span> */}
                </Modal.Header>
                <ModalBody>
                    <ul style={{ maxHeight: "50vh", overflow: "auto" }}>
                        {
                            openReservaData.registroDeCambiosDeEstado?.map(estado => (
                                <li><span className="fw-bold">{dayjs(estado.fecha).format("DD/MM/YYYY")}:</span> {
                                    estado.estado === "PENDIENTE" ? <span>Se creó la reserva (PENDIENTE)</span> : estado.estado === "CANCELADA" ? <span>Se canceló la reserva (CANCELADA)</span> : <span>Confirmaste la reserva (CONFIRMADA)</span>
                                }</li>
                            ))
                        }
                    </ul>
                </ModalBody>
            </Modal>}
        </div>
    )
}

const ConfirmButton = ({ reservaId, confirmStatusReserva }) => {
    const [loading, setLoading] = useState(false)

    function confirmReserva() {
        setLoading(true)
        axios.put(`http://localhost:8080/reservas/${reservaId}/confirm`, undefined, { withCredentials: true }).then(res => {
            if (res.status === 204) {
                confirmStatusReserva(reservaId)
            }
            setLoading(false)
        })
    }

    return (<Button onClick={() => confirmReserva()}>
        {loading ? <Spinner></Spinner> : "Confirmar"}
    </Button>)
}