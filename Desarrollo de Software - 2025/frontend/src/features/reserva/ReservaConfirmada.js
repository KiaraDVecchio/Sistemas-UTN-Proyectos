import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Container, Row, Col, Card, Button } from 'react-bootstrap';
import { moneda } from '../../components/alojamientos/AlojamientoCard';
import { MenuAccesible } from '../../components/MenuAccesible';
import './ReservaConfirmada.css';
import { useReserva } from './hooks/useReserva';

export function ReservaConfirmada() {
  const { id } = useParams();
  const navigate = useNavigate();

  const { loading, error, data: reserva, anfitrion } = useReserva(id);

  if (loading) return <div>Cargando...</div>;
  if (error || !reserva) return <div>Error al cargar la reserva</div>;

  const startDate = new Date(reserva.rangoFechas.fechaInicio);
  const endDate = new Date(reserva.rangoFechas.fechaFin);
  const diffInMs = endDate - startDate;
  const noches = Math.max(1, Math.ceil(diffInMs / (1000 * 60 * 60 * 24)));

  const precioPorNoche = +reserva.alojamiento.precioPorNoche || 0;
  const precioTotal = precioPorNoche * noches;
  const _moneda = moneda[reserva.alojamiento.moneda ?? "DOLAR_USA"];


  //ACA CAMBIE COSAS
  const handleDescargarComprobante = () => {
    // Simular descarga de comprobante
    const link = document.createElement('a');
    link.href = "data:text/plain;charset=utf-8," +
      encodeURIComponent(
        `COMPROBANTE DE RESERVA\n\n` +
        `Alojamiento: ${reserva.alojamiento.nombre}\n` +
        `Anfitrión: ${anfitrion.nombre}\n` +
        `Fechas: ${reserva.rangoFechas.fechaInicio.split("T")[0]} - ${reserva.rangoFechas.fechaFin.split("T")[0]}\n` +
        `Huéspedes: ${reserva.huespedes}\n` +
        `Total: $${precioTotal.toFixed(2)} ${_moneda}`
      );
    link.download = 'comprobante-reserva.txt';
    link.click();
  };

  const handleContactarAnfitrion = () => {
    const mensaje = `Hola ${reserva.alojamiento?.data?.anfitrion?.nombre}, acabo de reservar tu alojamiento "${reserva.alojamiento?.data?.nombre}" para las fechas ${reserva.fechaInicio} - ${reserva.fechaFin}. ¡Espero conocerte pronto!`;
    const mailtoLink = `mailto:anfitrion@ejemplo.com?subject=Consulta sobre reserva&body=${encodeURIComponent(mensaje)}`;
    window.open(mailtoLink, '_blank');
  };

  const handleIrAlInicio = () => {
    navigate('/');
  };
  return (
    <>
      <MenuAccesible items={[
        { label: "ir a detalles", anchor: 'detalles-reserva' },
        { label: "ir a acciones", anchor: 'acciones-reserva' },
        {
          label: "Ir al inicio", onClick: () => {
            navigate("/")
          }
        }
      ]} />

      <Container className="reserva-confirmada-container">
        <Row className="justify-content-center">
          <div className="reserva-confirmada-header">
            <div className="check-icon">
              <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="12" cy="12" r="10" fill="#22c55e" />
                <path d="m9 12 2 2 4-4" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            </div>
            <h1 className="reserva-confirmada-title">¡Reserva confirmada!</h1>
            <p className="reserva-confirmada-subtitle">
              Te enviamos el comprobante por correo
            </p>
          </div>

          <Card className="reserva-confirmada-card" id="detalles-reserva">
            <Card.Body>
              <div className="reserva-confirmada-detalle">
                <div className="reserva-imagen-section">
                  <img
                    src={`http://localhost:8080/images/${reserva.alojamientoSnapshot.fotos?.[0]?.path}`}
                    alt={reserva.alojamiento.fotos?.[0]?.description || 'Imagen del alojamiento'}
                    className="reserva-confirmada-image"
                  />
                </div>

                <div className="reserva-info-section">
                  <h3 className="alojamiento-nombre">{reserva.alojamiento.nombre}</h3>

                  <div className="detalle-reserva">
                    <div className="detalle-item">
                      <span className="detalle-label"> Anfitrión:</span>
                      <span className="detalle-valor">{anfitrion?.nombre}</span>
                    </div>

                    <div className="detalle-item">
                      <span className="detalle-label"> Dirección:</span>
                      <span className="detalle-valor">{`${reserva.alojamientoSnapshot?.direccion?.calle} ${reserva.alojamientoSnapshot?.direccion?.altura}` || 'No especificada'}</span>
                    </div>

                    <div className="detalle-item">
                      <span className="detalle-label"> Fechas:</span>
                      <span className="detalle-valor">{reserva.rangoFechas.fechaInicio.split("T")[0]} - {reserva.rangoFechas.fechaFin.split("T")[0]}</span>
                    </div>

                    <div className="detalle-item">
                      <span className="detalle-label"> Huéspedes:</span>
                      <span className="detalle-valor">{reserva.huespedes} {reserva.huespedes === '1' ? 'persona' : 'personas'}</span>
                    </div>

                    <div className="detalle-item">
                      <span className="detalle-label"> Noches:</span>
                      <span className="detalle-valor">{noches}</span>
                    </div>

                    <div className="detalle-item precio-total">
                      <span className="detalle-label">💰 Total pagado:</span>
                      <span className="detalle-valor">${precioTotal.toFixed(2)} {_moneda}</span>
                    </div>
                  </div>
                </div>
              </div>
            </Card.Body>
          </Card>

          <div className="reserva-confirmada-acciones" id="acciones-reserva">
            <Row>
              <Col xs={12} md={4} className="mb-3 mb-md-0">
                <Button
                  variant="primary"
                  className="btn-accion"
                  onClick={handleDescargarComprobante}
                >
                  📄 Descargar comprobante
                </Button>
              </Col>
              <Col xs={12} md={4} className="mb-3 mb-md-0">
                <Button
                  variant="outline-primary"
                  className="btn-accion"
                  onClick={handleContactarAnfitrion}
                >
                  💬 Contactar al anfitrión
                </Button>
              </Col>
              <Col xs={12} md={4} >
                <Button
                  variant="outline-secondary"
                  className="btn-accion"
                  onClick={handleIrAlInicio}
                >
                  🏠 Ir al menú de inicio
                </Button>
              </Col>
            </Row>
          </div>

          <div className="reserva-confirmada-footer">
            <p className="soporte-texto">
              ¿Dudas? Escribinos a <a href="mailto:soporte@birbnb.com">soporte@birbnb.com</a>
            </p>
          </div>
        </Row>
      </Container>
    </>
  );
}