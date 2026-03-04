import Carousel from 'react-bootstrap/Carousel';
import "./Comentarios.css";

const ComentariosMock = [
    {
        nombre: "Dariusz",
        pais: "Argentina",
        texto: `Lo primero que hice fue estar muy tranquilo, que era lo que necesitaba. El apartamento era muy bonito.`,
    },
    {
        nombre: "Gkosmo",
        pais: "Venezuela",
        texto: `Lugar muy íntimo, tranquilo, buena comida, servicio amable y hermosas habitaciones.`,
    },
    {
        nombre: "Alexdo1337",
        pais: "Chile",
        texto: `Gran casa nueva de madera a precio razonable, con sauna privada. Hermosa vista desde la ventana.`,
    },
];

export function Comentarios() {
    return (
        <>
            <h4>A los huéspedes que se alojaron acá les encantó:</h4>
            <Carousel tabIndex={1} indicators={false} controls={true} interval={null}>
                {ComentariosMock.map((t, idx) => (
                    <Carousel.Item key={idx}>
                        <div className="p-3 border rounded">
                            <div className="d-flex align-items-center mb-2">
                                <div className="rounded-circle bg-success text-white d-flex justify-content-center align-items-center" style={{ width: 40, height: 40 }}>
                                    {t.nombre[0]}
                                </div>
                                <div className="ms-2">
                                    <strong>{t.nombre}</strong><br />
                                    <small><span role="img" aria-label="flag"></span> {t.pais}</small>
                                </div>
                            </div>
                            <p style={{ fontStyle: "italic" }}>{`"${t.texto}"`}</p>
                        </div>
                    </Carousel.Item>
                ))}
            </Carousel>
        </>
    );
}
