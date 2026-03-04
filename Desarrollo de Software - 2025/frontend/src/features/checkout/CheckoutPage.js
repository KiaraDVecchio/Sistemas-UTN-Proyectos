import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import { Container, Row, Col, Card, Form, Button } from 'react-bootstrap';
import './checkoutPage.css';
import { Autenticacion } from '../autenticacion/Autenticacion';
import { useAlojamiento } from '../alojamiento/hooks/useAlojamiento';
import { moneda } from '../../components/alojamientos/AlojamientoCard';
import { useAuth } from '../../context/Auth';
import { MenuAccesible } from '../../components/MenuAccesible';
import { postReserva } from '../reserva/api/postReserva';

import { useFetchState } from "../../hooks/useFetchState.js";
import { useToast } from '../../context/Toast/index.js';



export default function CheckoutPage() {
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const alojamiento = useAlojamiento(id)

  const [selectedPayment, setSelectedPayment] = useState('full');
  const [showAuth, setShowAuth] = useState(false);


  const { state: fetchState, actions } = useFetchState();
  const { loading } = fetchState;


  // obtenemos fechas y huéspedes desde la URL
  const startStr = searchParams.get("start");
  const endStr = searchParams.get("end");
  const guests = searchParams.get("guests");
  const { isAuthenticated } = useAuth();

  function handleCloseAutenticacion() {
    setShowAuth(false)
  }

  const addToast = useToast()


  useEffect(() => {
    if ((alojamiento.data == null && alojamiento.loading === false) || !startStr || !endStr || !guests) {
      navigate('/', { replace: true });
    }
  }, [alojamiento, startStr, endStr, guests, navigate]);

  if (!alojamiento || !startStr || !endStr || !guests) return null;

  // calculamos noches a partir de fechas
  const startDate = new Date(startStr);
  const endDate = new Date(endStr);
  const diffInMs = endDate - startDate;
  const noches = Math.max(1, Math.ceil(diffInMs / (1000 * 60 * 60 * 24)));

  // obtenemos precio por noche desde alojamiento.price (ej. "$150 USD")
  const precioPorNoche = +alojamiento?.data?.precioPorNoche
  const precioTotal = precioPorNoche * noches;
  const _moneda = moneda[(alojamiento.data?.moneda) ?? "DOLAR_USA"]


  const handleContinue = async () => {
    if (!isAuthenticated) {
      setShowAuth(true);
      return;
    }

    actions.startLoading();

    try {
      const reserva = {
        alojamiento: alojamiento?.data?.id ?? id,
        rangoFechas: {
          desde: startStr,
          hasta: endStr,
        },
        huespedes: guests
      };

      const nuevaReserva = await postReserva(reserva);
      actions.receiveData(nuevaReserva);
      // addToast({
      //   type: "success",
      //   title: "Reserva exitosa"
      // });
      navigate(
        `/reserva-confirmada/${nuevaReserva.id}?start=${startStr}&end=${endStr}&guests=${guests}`,
        { replace: true }
      );
    } catch (error) {
      actions.startError();
      addToast({
        type: "danger",
        content: "No se pudo completar la reserva"
      })
    }
  };


  return (
    <>
      <MenuAccesible items={[
        { label: "ir a sección de pagar", anchor: 'checkout_form' },
        {
          label: "Volver al home", onClick: () => {
            navigate("/")
          }
        },
        {
          label: "Abrir menu de perfil", onClick: () => {
            const menu = document.getElementById("dropdown-basic")
            menu.click()
            menu.focus()
          }
        },
      ]} />
      <Container className="checkout-container">
        <Row className="gx-5">
          {/* === COLUMNA IZQUIERDA === */}
          <Col lg={7}>
            <h4 className="checkout-title">Solicitá una reserva</h4>



            {/* Pago */}
            <section className="mb-4">
              <h5>Elegí cómo pagar</h5>
              <Card
                className={`payment-card mb-3 ${selectedPayment === 'full' ? 'selected' : ''
                  }`}
                onClick={() => setSelectedPayment('full')}
              >
                <Card.Body className="d-flex align-items-center">
                  <Form.Check
                    tabIndex={0}
                    type="radio"
                    id="full"
                    name="payment"
                    className="me-3"
                    checked={selectedPayment === 'full'}
                  />
                  <div>
                    <strong>Pagá {precioTotal.toFixed(2)} {_moneda} ahora</strong>
                  </div>
                </Card.Body>
              </Card>

              <Card
                className={`payment-card ${selectedPayment === 'partial' ? 'selected' : ''
                  }`}
                onClick={() => setSelectedPayment('partial')}
              >
                <Card.Body>
                  <Form.Check
                    tabIndex={0}
                    type="radio"
                    id="partial"
                    name="payment_partial"
                    className="me-3"
                    checked={selectedPayment === 'partial'}
                  />
                  <div>
                    <strong>Pagá una parte ahora y otra más adelante</strong>
                    {selectedPayment === 'partial' && (
                      <p className="mt-2 mb-0 small">
                        Pagá ${(precioTotal * 0.2).toFixed(2)} {_moneda} hoy y ${(
                          precioTotal * 0.8
                        ).toFixed(2)} {_moneda} más tarde
                      </p>
                    )}
                  </div>
                </Card.Body>
              </Card>
            </section>

            {/* Continuar */}
            <section>
              {!isAuthenticated && (
                <p className="text-center text-muted mb-3">
                  Iniciá sesión o registrate para reservar
                </p>
              )}
              <Button
                variant="primary"
                className="btn-accion"
                onClick={handleContinue}
                disabled={loading}
              >
                {loading ? (
                  <>
                    <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                    Procesando...
                  </>
                ) : (
                  "Continuar"
                )}
              </Button>
            </section>
          </Col>

          {/* === COLUMNA DERECHA === */}
          <Col lg={5}>
            <Card className="checkout-summary-card shadow-sm">
              <Card.Img
                variant="top"
                src={alojamiento?.data?.fotos[0]?.path ?? "– sin foto –"}
                alt={alojamiento?.data?.fotos[0]?.description ?? "– sin nombre –"}
                className="summary-image"
              />
              <Card.Body>
                <Card.Title className="mb-1 summary-title">
                  {alojamiento?.data?.nombre ?? "– sin nombre –"}
                </Card.Title>

                <hr />
                <div className="d-flex justify-content-between mb-2">
                  <span>
                    ${precioPorNoche.toFixed(2)} {_moneda} x {noches} noches
                  </span>
                  <span>${precioTotal.toFixed(2)} {_moneda}</span>
                </div>
                <hr />
                {/* Fechas y huéspedes seleccionados */}
                <section className="mb-4">
                  <h5>Detalles de la estadía</h5>
                  <ul className='li-detalles'>
                    <li><strong>Desde:</strong> {startStr}</li>
                    <li><strong>Hasta:</strong> {endStr}</li>
                    <li><strong>Huéspedes:</strong> {guests}</li>
                  </ul>

                </section>

              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>

      {/* === MODAL LOGIN === */}
      {/* <Modal show={showAuth} onHide={() => setShowAuth(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Iniciá sesión para reservar</Modal.Title>
        </Modal.Header>
        <Modal.Body> */}
      <Autenticacion
        show={showAuth}
        onHide={handleCloseAutenticacion}
      />
      {/* </Modal.Body>
      </Modal> */}
    </>
  );
}
