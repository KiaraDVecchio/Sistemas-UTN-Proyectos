import { useState } from "react";
import "./formularioReserva.css";
import Header from "../../components/header/Header";

function FormularioReserva({ onSubmit }) {
  //useState agrega estado  a un componente
  const [nombre, setNombre] = useState("");
  const [email, setEmail] = useState("");
  const [fechaInicio, setFechaInicio] = useState("");
  const [fechaFin, setFechaFin] = useState("");
  const [cantPersonas, setCantPersonas] = useState(1); //que empiece en 1

  const handleSubmit = (e) => {
    //e de evento, contiene info sobre la accion del usuario
    e.preventDefault(); //si no ponemos esto cada vez que apretas reservar se recarga la pagina
    if (onSubmit) {
      onSubmit({
        nombre,
        email,
        fechaInicio,
        fechaFin,
        cantPersonas,
      });
    }
  };

  return (
    <>
      <div className="formulario-reserva">
        <form onSubmit={handleSubmit}>
          <div className="fila-doble">
            <div>
              <label className="form-label">Nombre: </label>
              <input
                className="form-input"
                type="text"
                value={nombre}
                onChange={(e) => setNombre(e.target.value)}
                placeholder="Tu nombre"
                required //obligatorio, se usa con form
              />
            </div>

            <div>
              <label className="form-label">Email: </label>
              <input
                className="form-input"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="ejemplo@gmail.com"
                required
              />
            </div>
          </div>

          <div className="fila-doble">
            <div>
              <label className="form-label">Fecha de inicio: </label>
              <input
                className="form-input"
                type="date"
                value={fechaInicio}
                onChange={(e) => setFechaInicio(e.target.value)}
                required
              />
            </div>

            <div>
              <label className="form-label">Fecha de fin: </label>
              <input
                className="form-input"
                type="date"
                value={fechaFin}
                onChange={(e) => setFechaFin(e.target.value)}
                required
              />
            </div>
          </div>

          <div>
            <label className="form-label">Cantidad de personas: </label>
            <input
              className="form-input"
              type="number"
              min="1"
              value={cantPersonas}
              onChange={(e) => setCantPersonas(e.target.value)}
              required
            />
          </div>

          <button type="submit" className="botonDeReservar">
            Reservar
          </button>
        </form>
      </div>
    </>
  );
}

export default FormularioReserva;
