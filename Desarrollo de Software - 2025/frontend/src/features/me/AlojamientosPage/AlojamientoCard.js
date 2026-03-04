import { Card, CardBody, CardFooter, } from "react-bootstrap"
import { useNavigate } from "react-router-dom"

export const AlojamientoCard = ({ alojamiento }) => {
    const navigate = useNavigate()

    return <Card onClick={() => {
        navigate("/me/admin/alojamientos/" + alojamiento.id)
    }} className="alojamiento-card" style={{ height: '400px', width: "300px", flexShrink: 0, overflow: "hidden", cursor: "pointer" }}>
        <CardBody style={{ padding: 0, overflow: "hidden" }}>
            <div style={{ overflow: "hidden", width: "100%", height: "100%" }}>
                <img style={{ objectFit: "cover", width: "100%", height: "100%" }} src={`http://localhost:8080/images/${alojamiento.fotos[0]?.path}`} alt={alojamiento.fotos[0]?.description} />
            </div>
        </CardBody>
        <CardFooter>
            <p style={{ width: "100%", textOverflow: "ellipsis", whiteSpace: "nowrap", overflow: "hidden" }}>{alojamiento.nombre}</p>
        </CardFooter>
    </Card>
}