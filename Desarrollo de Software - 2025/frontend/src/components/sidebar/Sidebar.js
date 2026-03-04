import { useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useEffect } from "react";
import "./sidebar.css";
import Col from "react-bootstrap/Col";
import Form from "react-bootstrap/Form";
import { Amenities } from "../alojamientos/Amenities";
import { Host } from "../alojamientos/Host";
import { moneda } from "../alojamientos/AlojamientoCard";
import { useFetchState } from "../../hooks/useFetchState.js";
import DatePicker from "../DatePicker.js";
import DateRangePicker from "../DateRangePicker.js";
import dayjs from "dayjs";
import axios from "axios";
//import { reservasExistentes } from "../../features/alojamiento/api/getReservasByAlojamiento"; 



export function Sidebar({ alojamiento }) {
  const navigate = useNavigate();
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [huespedes, setHuespedes] = useState(1);
  const [errorMessage, setErrorMessage] = useState("");
  const [showError, setShowError] = useState(false);
  const [fechasOcupadas, setFechasOcupadas] = useState([]);
  const { state: fetchState, actions } = useFetchState();
  const { loading } = fetchState;


  useEffect(() => {
    axios.get(`http://localhost:8080/alojamientos/${alojamiento.id}/unavailable-dates`).then(res => {
      if (res.status === 200) {
        setFechasOcupadas(res.data.data);
      }
    }).catch(function (error) {
      console.error(error);
    });
  }, [alojamiento])

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

  const handleReserve = () => {

    setErrorMessage("");
    setShowError(false);

    // Validar que ambas fechas estén seleccionadas
    if (!startDate || !endDate) {
      setErrorMessage("Completar fechas de llegada y salida para continuar.");
      setShowError(true);
      setTimeout(() => {
        setShowError(false);
        setTimeout(() => {
          setErrorMessage("");
        }, 300);
      }, 3000);

      return;
    }


    // Validar que la fecha de llegada no sea anterior a hoy
    const today = getTodayDate();
    if (startDate < today) {
      setErrorMessage("La fecha de llegada no puede ser anterior a hoy.");
      setShowError(true);
      setTimeout(() => {
        setShowError(false);
        setTimeout(() => {
          setErrorMessage("");
        }, 300);
      }, 3000);
      return;
    }

    // Validar que la fecha de salida sea posterior a la fecha de llegada
    if (endDate <= startDate) {
      setErrorMessage("La fecha de salida debe ser posterior a la fecha de llegada.");
      setShowError(true);
      setTimeout(() => {
        setShowError(false);
        setTimeout(() => {
          setErrorMessage("");
        }, 300);
      }, 3000);
      return;
    }

    // Si todas las validaciones pasan, navegar a la reserva

    setErrorMessage("");
    navigate(
      `/reserva/${alojamiento.id}?start=${startDate}&end=${endDate}&guests=${huespedes}`
    );
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

  if (!alojamiento) return <div>Alojamiento no encontrado</div>;

  return (
    <>
      <Col md={4} style={{ paddingRight: "none" }} className="sidebar-col">
        <div id="alojamiento_form" className="sidebar">
          {/* Precio y huéspedes */}
          <div className="price-box mb-4">
            <div className="price">
              <span className="amount">{alojamiento.precioPorNoche}</span>
              <span className="currency">{moneda[alojamiento.moneda]}</span>
              <small className="per-night">/noche</small>
            </div>
          </div>

          <div>
            {/* Fechas de reserva */}
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

            {/* Cantidad de huéspedes */}
            <Form.Group controlId="huespedesSelect" className="mb-3">
              <Form.Label>Huéspedes</Form.Label>
              <Form.Control
                as="select"

                onChange={(e) => setHuespedes(+e.target.value)}
              >
                {[...Array(alojamiento.cantHuespedesMax).keys()].map((n) => (
                  <option key={n + 1} value={n + 1}>
                    {n + 1}
                  </option>
                ))}
              </Form.Control>
            </Form.Group>

            <button
              className="botonDeReservar"
              onClick={handleReserve}
              disabled={loading}
            >
              {loading ? "Procesando..." : "Reservar ahora"}
            </button>

            {/* Mensaje de error */}
            {(errorMessage || showError) && (
              <div className={`error-message ${showError ? 'error-message-show' : ''}`}>
                {errorMessage}
              </div>
            )}

          </div>
        </div>
      </Col>
    </>
  );
}
