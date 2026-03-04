import React, { useEffect, useMemo, useState } from "react";
import "./reservasUsuario.css";
import { Card, Button, Modal, Form } from "react-bootstrap";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useReservaDelUsuario } from "./hooks/useReservaDelUsuario";
import { deleteReservaById } from "./api/deleteReservaById";
import dayjs from "dayjs";
import { Pager } from "../../components/Pager";
import DatePicker from "../../components/DatePicker";
import { useFetchState } from "../../hooks/useFetchState";
import axios from "axios";
import DateRangePicker from "../../components/DateRangePicker";
import { useToast } from "../../context/Toast";


export function ReservasUsuario() {

  // ========= PAGINACION ==========
  const [params] = useSearchParams();
  const page = Number.isNaN(Number(params.get("page") ?? undefined))
    ? 1
    : Number(params.get("page"));

  // ======== REQUEST Y DATOS ===========
  const { state: { loading, error, data }, actions: { cancelarReserva, updateReserva } } = useReservaDelUsuario(page);
  const reservas = data?.data
  const totalPages = data?.total_pages ?? 1

  const [showModal, setShowModal] = useState(false);
  const [reservaSeleccionada, setReservaSeleccionada] = useState(null);
  const [openEditReserva, setOpenEditReserva] = useState(null);

  const navigate = useNavigate();

  // ========== HANDLERS ==========
  const calcularNoches = (desde, hasta) => {
    const d = new Date(desde);
    const h = new Date(hasta);
    return Math.round((h - d) / (1000 * 60 * 60 * 24));
  };

  const handleCancelarClick = (reserva) => {
    setReservaSeleccionada(reserva);
    setShowModal(true);
  };

  function handleConfirmarCancel() {
    deleteReservaById(reservaSeleccionada.id).then((res) => {
      if (res === 204) {
        cancelarReserva(reservaSeleccionada.id)
      }
      setShowModal(false);
      setReservaSeleccionada(null);
    })
  };

  const handleCerrarModal = () => {
    setShowModal(false);
    setReservaSeleccionada(null);
  };


  return (
    <>
      <div className="reservas-body">
        <h2 style={{ margin: "16px auto" }}>Mis reservas</h2>
        {reservas != null && reservas.length > 0 ? (
          <>
            {reservas.map((reserva, idx) => {
              const { alojamientoSnapshot: alojamiento, rangoFechas, estado } = reserva;
              const noches = calcularNoches(rangoFechas.fechaInicio, rangoFechas.fechaFin);
              const total = noches * (alojamiento?.precioPorNoche ?? 0);

              if (alojamiento == null) return null
              const disabled = !dayjs(rangoFechas.fechaInicio).isAfter(dayjs()) || reserva.estado !== "PENDIENTE"
              const disabledModificar = !dayjs(rangoFechas.fechaInicio).isAfter(dayjs()) || reserva.estado === "CANCELADA"
              return (
                <Card key={idx} className="card-reservas bg-dark text-white mb-4">
                  <div
                    className="clickable-card"
                    style={{ cursor: "pointer" }}
                  >
                    <Card.Img
                      className="card-image-reservas"
                      src={alojamiento.fotos?.[0]?.path}
                      alt={alojamiento.nombre}
                    />
                    <Card.ImgOverlay className="d-flex flex-column justify-content-between">
                      <Card.Title className="text-shadow">
                        {alojamiento.nombre}
                      </Card.Title>
                      <div className="d-flex justify-content-between align-items-end w-100">
                        <div className="info-fechas">
                          <div>
                            <strong>
                              {new Date(rangoFechas.fechaInicio).toLocaleDateString()} –{" "}
                              {new Date(rangoFechas.fechaFin).toLocaleDateString()}
                            </strong>
                          </div>
                          <div>
                            {noches} noches · {estado}
                          </div>
                        </div>
                        <div className="info-accion">
                          <div className="total">
                            Total: ${total.toLocaleString()} {alojamiento.moneda}
                          </div>
                          <Button
                            disabled={disabledModificar}
                            className="me-2"
                            variant={disabledModificar ? "dark" : "success"}
                            size="sm"
                            onClick={(e) => {
                              e.stopPropagation()
                              if (!disabledModificar) {
                                setOpenEditReserva(reserva)
                              }
                            }}
                          >
                            Modificar reserva
                          </Button>
                          <Button
                            disabled={disabled}
                            variant={!disabled ? "danger" : "dark"}
                            size="sm"
                            onClick={(e) => {
                              e.stopPropagation(); // Esto es para evitar que propague el click
                              if (!disabled) {
                                handleCancelarClick(reserva);
                              }
                            }}
                          >
                            Cancelar reserva
                          </Button>
                        </div>
                      </div>
                    </Card.ImgOverlay>
                  </div>
                </Card>
              );
            })}
            <Pager totalPages={totalPages} />
          </>
        ) : (
          <div className="sin-reservas">
            <p>No tenés reservas.</p>
            <Button style={{ maxWidth: "300px" }} onClick={() => { navigate("/") }}>Mirá los alojamientos disponibles y hacé una reserva</Button>
          </div>
        )}
      </div>

      {/* Modal de confirmación */}
      <Modal show={showModal} onHide={handleCerrarModal} centered>
        <Modal.Header closeButton>
          <Modal.Title>Confirmar cancelación</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {reservaSeleccionada && (
            <>
              ¿Estás seguro que querés cancelar la reserva en{" "}
              <strong>{reservaSeleccionada.alojamiento.nombre}</strong> del{" "}
              {new Date(
                reservaSeleccionada.rangoFechas.fechaInicio
              ).toLocaleDateString()}{" "}
              al{" "}
              {new Date(
                reservaSeleccionada.rangoFechas.fechaFin
              ).toLocaleDateString()}
              ?
            </>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={handleCerrarModal}>
            Volver
          </Button>
          <Button variant="danger" onClick={handleConfirmarCancel}>
            Sí, cancelar reserva
          </Button>
        </Modal.Footer>
      </Modal>
      {openEditReserva != null &&
        <EditFechasReservaModal open={openEditReserva} setOpen={setOpenEditReserva} reserva={openEditReserva} updateReserva={updateReserva} />
      }

    </>
  );
}


const EditFechasReservaModal = ({ open, setOpen, reserva, updateReserva }) => {


  const [startDate, setStartDate] = useState(dayjs(reserva.rangoFechas.fechaInicio).toDate());
  const [endDate, setEndDate] = useState(dayjs(reserva.rangoFechas.fechaFin).toDate());
  const [fechasOcupadas, setFechasOcupadas] = useState([]);
  const { state: fetchState, actions } = useFetchState();

  const addToast = useToast()

  useEffect(() => {
    setFechasOcupadas([])
    axios.get(`http://localhost:8080/alojamientos/${reserva.alojamiento}/unavailable-dates`).then(res => {
      if (res.status === 200) {
        setFechasOcupadas(res.data.data.filter(el => el[0] !== reserva.rangoFechas.fechaInicio || el[1] !== reserva.rangoFechas.fechaFin));
      }
    }).catch(function (error) {
      console.error(error);
    });
  }, [reserva.alojamientoSnapshot.id])
  // Obtengo fecha actual
  const getTodayDate = () => {
    const today = new Date();
    return today.toISOString().split('T')[0];
  };

  const handleStartDateChange = (e) => {
    const selectedStartDate = e;
    setStartDate(selectedStartDate);
    setEndDate("");
  };

  const handleEndDateChange = (e) => {
    setEndDate(e);
  };

  const rangoFechasOcupadas = useMemo(() => (
    fechasOcupadas?.map(el => ({ start: new Date(el[0].split("T")[0]), end: new Date(el[1].split("T")[0]) })) ?? []
  ), [fechasOcupadas])

  const maxDate = useMemo(() => startDate != null
    ? rangoFechasOcupadas.reduce((prev, rango) => {
      if (prev == null && new Date(startDate).getTime() < rango.start.getTime()) return rango.start
      if (prev != null && rango.start.getTime() < prev.getTime() && new Date(startDate).getTime() < rango.start.getTime()) return rango.start
    }, null)
    : undefined, [startDate, rangoFechasOcupadas])


  function handlePostEdit() {
    actions.startLoading()
    axios.put("http://localhost:8080/reservas/" + reserva.id, ({
      desde: startDate.toISOString(),
      hasta: endDate.toISOString(),
    }), { withCredentials: true }).then((res) => {
      if (res.status === 200) {
        actions.receiveData(null)
        addToast({
          content: "Reserva modificada con éxito",
          type: "success"
        })
        updateReserva(res.data)
      } else {
        addToast({
          content: "Fallo al modificar la reserva",
          type: "danger"
        })
        actions.startError()
      }
    }).catch(() => {
      addToast({
        title: "Fallo al modificar la reserva",
        type: "danger"
      })
      actions.startError()
    })
  }

  return <Modal show={open} onHide={() => setOpen(null)} centered>
    <Modal.Header closeButton>
      <Modal.Title>Modificar reserva</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      {reserva && (
        <>
          <div>
            ¿Desea modificar la reserva de{" "} <strong>{reserva.alojamientoSnapshot.nombre}</strong>
          </div>
          <Form.Group controlId="fechaStart" className="mb-3">
            <Form.Label>Fecha de llegada</Form.Label>
            <DatePicker disabledDatesIntervals={rangoFechasOcupadas} value={startDate} minDate={getTodayDate()} setValue={handleStartDateChange} />
          </Form.Group>

          <Form.Group controlId="fechaEnd" className="mb-3">
            <Form.Label>Fecha de salida</Form.Label>
            <DateRangePicker
              fechaInicio={startDate}
              fechaMaxima={maxDate}
              setValue={handleEndDateChange}
              value={endDate}
            />
          </Form.Group>
        </>
      )}
    </Modal.Body>
    <Modal.Footer>
      <Button variant="outline-secondary" onClick={() => setOpen(null)}>
        Volver
      </Button>
      <Button variant="success" onClick={handlePostEdit}>
        Sí, cancelar reserva
      </Button>
    </Modal.Footer>
  </Modal>
}