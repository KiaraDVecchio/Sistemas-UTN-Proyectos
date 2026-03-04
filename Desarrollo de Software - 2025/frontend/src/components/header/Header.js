import "./Header.css";
import logo from "../../logo.svg";
import Filters from "../filters/Filters";
import { useNavigate } from "react-router-dom";
import React, { useState } from "react";
import Dropdown from "react-bootstrap/Dropdown";
import { Autenticacion } from "../../features/autenticacion/Autenticacion";
import { useAuth } from "../../context/Auth";
import Button from "react-bootstrap/Button";
import Modal from "react-bootstrap/Modal";
import { Notificaciones } from "../notificaciones/Notificaciones.js";

function Header({ onSearch, filters }) {
  const { isAuthenticated, signOut, user } = useAuth();
  const [showAutenticacion, setShowAutenticacion] = useState(false);
  const [showFilters, setShowFilters] = useState(false);
  const navigate = useNavigate();

  const handleAuthClick = () => setShowAutenticacion(true);
  const handleCloseAutenticacion = () => setShowAutenticacion(false);
  const handleMisReservas = () => navigate("/me/reservas");
  const handleDashboard = () => navigate("/me/admin/dashboard");

  const handleClickLogo = () => navigate("/");

  return (
    <>
      <header className="header">
        <div className="header-section left">
          <img
            src={logo}
            alt="Birbnb logo"
            width={120}
            height={110}
            onClick={handleClickLogo}
            style={{ cursor: "pointer" }}
          />
        </div>

        {/* -- Escritorio: muestro Filters -- */}
        {filters && (
          <div className="header-section center d-desktop">
            <Filters onSearch={onSearch} />
          </div>
        )}

        {/* -- Móvil: muestro solo el botón -- */}
        {filters && (
          <div className="header-section center d-mobile">
            <Button
              className="botonFiltros"
              onClick={() => setShowFilters(true)}
            >
              <svg
                className="lupaFiltros"
                xmlns="http://www.w3.org/2000/svg"
                width="16"
                height="16"
                fill="#1B4079"
                viewBox="0 0 24 24"
              >
                <path d="M10 2a8 8 0 105.293 14.293l4.707 4.707 1.414-1.414-4.707-4.707A8 8 0 0010 2zm0 2a6 6 0 110 12 6 6 0 010-12z" />
              </svg>
            </Button>
          </div>
        )}
        <div className="header-section right">
          {isAuthenticated && !!user ? (
            <Notificaciones></Notificaciones>
          ) :
            <></>
          }
          {!user ? (
            <Button className="call-to-action-button" onClick={handleAuthClick}>
              ¡Inicia sesión o registrate!
            </Button>
          ) : (
            <Dropdown>
              <Dropdown.Toggle id="user-menu-dropdown-basic">
                ☰
              </Dropdown.Toggle>
              <Dropdown.Menu>
                <div>
                  <p style={{ padding: "0 16px", fontWeight: 600, margin: 0 }}>{user.nombre}</p>
                  <p style={{ padding: "0 16px", fontWeight: 300, color: "#666", margin: 0 }}>{user.tipo?.toLowerCase()}</p>
                </div>
                <Dropdown.Divider />
                {user.tipo === 'ANFITRION' && (
                  <Dropdown.Item onClick={handleDashboard}>
                    Dashboard de anfitrión
                  </Dropdown.Item>)}
                <Dropdown.Item onClick={handleMisReservas}>
                  Mis reservas
                </Dropdown.Item>
                <Dropdown.Item onClick={signOut}>Cerrar sesión</Dropdown.Item>
              </Dropdown.Menu>
            </Dropdown>
          )}
        </div>
      </header>

      {/* --- Modal de filtros para móvil --- */}
      <Modal show={showFilters} onHide={() => setShowFilters(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Filtros de búsqueda</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {/* Reutilizo tu componente Filters: pasándole onSearch y 
              un callback para cerrar el modal tras buscar */}
          <Filters
            onSearch={(filtroValues) => {
              onSearch && onSearch(filtroValues);
              setShowFilters(false);
            }}
          />
          <div className="mt-2 text-right"></div>
        </Modal.Body>
      </Modal>

      <Autenticacion
        show={showAutenticacion}
        onHide={handleCloseAutenticacion}
      />
    </>
  );
}

export default Header;
