import "./AlojamientoCard.css";
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import { useFetchState } from "../../hooks/useFetchState.js";

export const moneda = {
  DOLAR_USA: "USD",
  PESO_ARG: "ARS",
  REALES: "RR"
}
export default function AlojamientoCard({ aAlojamiento }) {
  const navigate = useNavigate();
  const { state: fetchState, actions } = useFetchState();
  const { loading } = fetchState;

  const handleClick = () => {
    actions.startLoading();
    navigate(`/alojamientos/${aAlojamiento.id}`);

    //si queremos simular un delay para ver que funcionar el customHook =>

    /*setTimeout(() => {
      actions.receiveData(); 
      navigate(`/alojamientos/${aAlojamiento.id}`);
    }, 500);*/
  };

  return (
    <div
      className="alojamiento__card alojamiento-card"
      onClick={handleClick}
      style={{ cursor: loading ? "wait" : "pointer" }}
    >
      <div style={{ width: "100%", overflow: "hidden", borderRadius: "8px" }}>
        <img
          style={{ objectFit: "cover", width: "100%" }}
          src={`http://localhost:8080/images/${aAlojamiento.fotos?.[0]?.path}`}
          alt={aAlojamiento.fotos?.[0]?.description}
          className="card-image"
        />
      </div>

      <div className="card-footer">
        <h5 className="card-title">{aAlojamiento.nombre}</h5>
        <span className="card-price">
          <strong>{moneda[aAlojamiento.moneda]} {aAlojamiento.precioPorNoche}</strong> por noche
        </span>

        {loading && (
          <div className="card-loading">
            <span>Cargando...</span>
          </div>
        )}
      </div>
    </div>
  );
}

