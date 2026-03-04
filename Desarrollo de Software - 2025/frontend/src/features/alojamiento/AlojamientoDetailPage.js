import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import "./alojamientoDetailPage.css";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";
import { Sk } from "react-bootstrap"
import { Amenities } from "../../components/alojamientos/Amenities";
import { Host } from "../../components/alojamientos/Host";
import { Comentarios } from "./Comentarios";

import { Gallery } from "../../components/alojamientos/Gallery";
import { Sidebar } from "../../components/sidebar/Sidebar";
import { useAlojamiento } from "./hooks/useAlojamiento";
import { AlojamientoSkeleton } from "./Skeleton";
import { MenuAccesible } from "../../components/MenuAccesible";
import Col from "react-bootstrap/Col";


export function AlojamientoDetailPage() {

  const [showModal, setShowModal] = useState(false);
  const [selectedImage, setSelectedImage] = useState("");
  const [huespedes, setHuespedes] = useState(1);

  const navigate = useNavigate()

  const alojamiento = useAlojamiento()

  const [ciudades, setCiudades] = useState([])

  const [ciudadName, setCiudadName] = useState('')

  useEffect(() => {
    fetch("http://localhost:8080/generaldata/paises/6852c769e0971a3dc7ba4798/localidades").then(res => res.json())
      .then(res => {
        setCiudades(res.data)
      })
  }, [])

  useEffect(() => {
    if (alojamiento.data?.direccion?.ciudad != null) {
      const nombre = ciudades.find(el => el.id === alojamiento.data?.direccion?.ciudad)?.nombre
      setCiudadName(nombre ?? '')
    }
  }, [ciudades, alojamiento])

  const direccionQuery = useMemo(() => encodeURIComponent(`${alojamiento.data?.direccion?.calle} ${alojamiento.data?.direccion?.altura}, ${ciudadName}`), [alojamiento.data, ciudadName])

  return (
    <>
      <MenuAccesible items={[
        { label: "ir a fotos", anchor: 'alojamiento_fotos' },
        { label: "ir al formulario", anchor: 'alojamiento_form' },
        {
          label: "Volver al inicio", onClick: () => {
            navigate("/")
          }
        },
        {
          label: "Abrir menu de perfil", onClick: () => {
            const menu = document.getElementById("dropdown-basic")
            menu.click()
            menu.focus()
          }
        }
      ]} />
      <Container className=".container-sm detail-aloj">
        <h3 className="mt-4">{alojamiento?.data?.nombre ?? "– sin nombre –"}</h3>

        {alojamiento.loading && <AlojamientoSkeleton />}
        {alojamiento.data != null
          ?
          <>
            <Row className="content-row">
              <Gallery alojamiento={alojamiento.data} />
              <Sidebar alojamiento={alojamiento.data} />
            </Row>
            <Row className="alojamiento-detail-datarow">
              <Col style={{ minWidth: "450px" }}>
                <Amenities alojamiento={alojamiento.data} />
                <hr />
                <h5>Anfitrión</h5>
                <Host alojamiento={alojamiento.data} />
                <hr />
                <Comentarios />
              </Col>
              <Col md={4} style={{ minWidth: "450px" }}>
                <iframe style={{ width: "100%" }} title="mapa" src={`https://www.google.com/maps?q=${direccionQuery}&output=embed`} width="500" height="450" allowFullScreen loading="lazy" referrerPolicy="no-referrer-when-downgrade"></iframe>
              </Col>

            </Row>
          </>
          : <div>Alojamiento no encontrado</div>}
      </Container>




    </>
  );
}
