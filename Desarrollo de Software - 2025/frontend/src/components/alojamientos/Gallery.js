import Row from "react-bootstrap/Row";
import Col from "react-bootstrap/Col";
import { useState } from "react";
import "./Gallery.css";
import Modal from "react-bootstrap/Modal";
import { clickOnEnterKeyDown } from "../../utils/clickOnEnterKeyDown";

export function Gallery({ alojamiento }) {

  const [showModal, setShowModal] = useState(false);
  const [selectedImage, setSelectedImage] = useState("");
  const [huespedes, setHuespedes] = useState(1);

  const fotos = alojamiento.fotos

  const handleClick = (src) => {
    setSelectedImage(src);
    setShowModal(true);
  };

  return (
    <>
      <Col id="alojamiento_fotos" md={8} className="gallery-col">
        <Row className="fila">
          {fotos.slice(0, 2).map((foto, i) => (
            <Col tabIndex={0} key={i} onKeyDown={clickOnEnterKeyDown} onClick={() => handleClick(foto.path)}>
              <img src={`http://localhost:8080/images/${foto.path}`} alt={`foto ${foto.description}`} />
            </Col>
          ))}
        </Row>
        <Row className="fila">
          {fotos.slice(2).map((foto, i) => (
            <Col tabIndex={0} key={i} onKeyDown={clickOnEnterKeyDown} onClick={() => handleClick(foto.path)}>
              <img src={`http://localhost:8080/images/${foto.path}`} alt={`foto: ${foto.description}`} />
            </Col>
          ))}
        </Row>
      </Col>

      {/* Lightbox Modal */}
      <Modal
        show={showModal}
        onHide={() => setShowModal(false)}
        centered
        size="md"
      >
        <Modal.Body className="p-0">
          <img
            src={`http://localhost:8080/images/${selectedImage}`}
            alt="ampliada"
            style={{
              width: "100%",
              height: "auto",
              maxHeight: "80vh",
              objectFit: "cover",
            }}
          />
        </Modal.Body>
      </Modal>
    </>
  );
}
