import "./Filters.css";
import React, { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";

function Filters({ onSearch }) {
  const [searchParams, setSearchParams] = useSearchParams();
  // estado de los inputs
  const [lugar, setLugar] = useState("");
  const [viajeros, setViajeros] = useState("");
  const [min, setMin] = useState("");
  const [max, setMax] = useState("");
  const [caracteristicas, setCaracteristicas] = useState("");

  useEffect(() => {
    setLugar(searchParams.get("ubicacion") || "");
    setViajeros(searchParams.get("cantHuespedesPermitidos") || "");
    setMin(searchParams.get("precioMinimo") || "");
    setMax(searchParams.get("precioMaximo") || "");
    setCaracteristicas(searchParams.get("caracteristicasEspeciales") || "");
  }, [searchParams]);

  const handleSearch = () => {
    const filters = {};

    if (lugar.trim()) filters.ubicacion = lugar.trim();
    if (viajeros) filters.cantHuespedesPermitidos = viajeros;
    if (min) filters.precioMinimo = min;
    if (max) filters.precioMaximo = max;
    if (caracteristicas.trim())
      filters.caracteristicasEspeciales = caracteristicas.trim();

    // Resetear página a 1 cuando se aplican filtros
    filters.page = "1";

    // Actualizar URL con los nuevos filtros
    setSearchParams(filters);
    if (onSearch) {
      onSearch({
        lugar,
        capacidad: viajeros,
        min,
        max,
        caracteristicas,
      });
    }
  };
  //para limpiaar los filtros
  const handleClearFilters = () => {
    setLugar("");
    setViajeros("");
    setMin("");
    setMax("");
    setCaracteristicas("");
    setSearchParams({});
  };

  return (
    <nav className="navbar" id="filtros">
      <ul className="lista-desordenada">
        <li className="nav-li">
          <p className="nav-seccion">Lugar</p>
          <input
            className="nav-input"
            placeholder="Explorar destinos"
            value={lugar}
            onChange={(e) => setLugar(e.target.value)}
          />
        </li>
        <li className="nav-li">
          <p className="nav-seccion">Viajeros</p>
          <input
            className="nav-input"
            type="number"
            placeholder="¿Cuántos?"
            value={viajeros}
            min={0}
            onChange={(e) => setViajeros(e.target.value)}
          />
        </li>
        <li className="rango-precio">
          <p className="nav-seccion">Rango de precio</p>
          <div className="input-group">
            <input
              className="nav-input"
              type="number"
              min={0}
              placeholder="Mín"
              value={min}
              onChange={(e) => setMin(e.target.value)}
            />
            <span>–</span>
            <input
              className="nav-input"
              type="number"
              min={0}
              placeholder="Máx"
              value={max}
              onChange={(e) => setMax(e.target.value)}
            />
          </div>
        </li>
        <li className="nav-li">
          <p className="nav-seccion">Características</p>
          <select
            className="nav-input"
            value={caracteristicas}
            onChange={(e) => setCaracteristicas(e.target.value)}
          >
            <option value="">Seleccione</option>
            <option value="WIFI">Wifi</option>
            <option value="PISCINA">Piscina</option>
            <option value="MASCOTAS_PERMITIDAS">Mascotas permitidas</option>
            <option value="ESTACIONAMIENTO">Estacionamiento</option>
          </select>
        </li>
      </ul>

      <button className="search-button" onClick={handleSearch}>
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="16"
          height="16"
          fill="white"
          viewBox="0 0 24 24"
        >
          <path d="M10 2a8 8 0 105.293 14.293l4.707 4.707 1.414-1.414-4.707-4.707A8 8 0 0010 2zm0 2a6 6 0 110 12 6 6 0 010-12z" />
        </svg>
      </button>
    </nav>
  );
}

export default Filters;
