import React, { useEffect, useState } from 'react';
import {
    Form,
    Button,
    Row,
    Col,
    InputGroup,
    Card
} from 'react-bootstrap';
import { Select } from '../../../../components/shared/Select';
import { useNavigate, useParams } from 'react-router-dom';
import { postAlojamiento } from '../api/postAlojamiento';
import { getAlojamientoById } from '../../../alojamiento/api/getAlojamientoById';
import { useToast } from '../../../../context/Toast';

export const EditAlojamientoPage = () => {

    const params = useParams()
    const toast = useToast()
    const navigate = useNavigate()

    const alojamientoId = params.id

    const [images, setImages] = useState([])
    const [ciudades, setCiudades] = useState([])
    const [caracteriticasSeleccionadas, setCaracteristicasSeleccionadas] = useState([])
    const [ciudadSeleccionada, setCiudadSeleccionada] = useState()

    const [formData, setFormData] = useState({
        nombre: '',
        descripcion: '',
        precio: '',
        moneda: 'PESO_ARG',
        checkin: '',
        checkout: '',
        huespedes: '',
        calle: '',
        altura: '',
    });


    useEffect(() => {
        fetch("http://localhost:8080/generaldata/paises/6852c769e0971a3dc7ba4798/localidades").then(res => res.json())
            .then(res => {
                setCiudades(res.data)
            })
    }, [])

    useEffect(() => {
        if (alojamientoId != null) {
            getAlojamientoById(alojamientoId).then(res => {
                const alojamientoData = res.data
                if (alojamientoData != null) {
                    setFormData({
                        nombre: alojamientoData.nombre,
                        descripcion: alojamientoData.descripcion,
                        precio: alojamientoData.precioPorNoche,
                        moneda: alojamientoData.moneda,
                        checkin: alojamientoData.checkin,
                        checkout: alojamientoData.checkout,
                        huespedes: alojamientoData.cantHuespedesMax,
                        calle: alojamientoData.direccion?.calle,
                        altura: alojamientoData.direccion?.altura,
                    })
                    setCaracteristicasSeleccionadas(alojamientoData.caracteristicas)
                    setCiudadSeleccionada(alojamientoData.direccion.ciudad)
                    setImages(alojamientoData.fotos)
                }
            })
        }
    }, [alojamientoId])


    const caracteristicasDisponibles = [
        "WIFI",
        "PISCINA",
        "MASCOTAS_PERMITIDAS",
        "ESTACIONAMIENTO"
    ];

    const monedas = ['DOLAR_USA', 'PESO_ARG', 'REALES'];

    const handleChange = (e) => {
        const { name, value, type, selectedOptions } = e.target;
        if (type === 'select-multiple') {
            const values = Array.from(selectedOptions).map((o) => o.value);
            setFormData(prev => ({ ...prev, [name]: values }));
        } else {
            setFormData(prev => ({ ...prev, [name]: value }));
        }
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        const _formData = new FormData()

        _formData.append("nombre", formData.nombre)
        _formData.append("descripcion", formData.descripcion)
        _formData.append("precioPorNoche", formData.precio)
        _formData.append("moneda", formData.moneda)
        _formData.append("horarioCheckIn", formData.checkin)
        _formData.append("horarioCheckOut", formData.checkout)
        _formData.append("cantHuespedesMax", formData.huespedes)
        _formData.append("caracteristicas", JSON.stringify(caracteriticasSeleccionadas))
        _formData.append("direccion", JSON.stringify({
            "calle": formData.calle, "altura": formData.altura,
            ciudad: ciudadSeleccionada
        }))
        images.filter(el => el instanceof File).forEach(image => {
            _formData.append("fotos", image)
        })
        images.filter(el => typeof el === 'object' && 'path' in el).forEach(image => {
            _formData.append("keep", image.path)
        })

        postAlojamiento(_formData, alojamientoId).then((res) => {
            toast({
                type: "success",
                content: alojamientoId != null ? "Modificado con éxito" : "Creado con éxito"
            })
            navigate("/me/alojamientos")
        })
        // })

    };


    return <div style={{ minHeight: "85vh", padding: "32px" }}>
        <h1>{alojamientoId == null ? 'Crear alojamiento' : 'Editar alojamiento'}</h1>
        <Row>
            <Col>
                <Card style={{ height: "100%", display: "flex", flexDirection: "column" }}>
                    <input onChange={(evt) => {
                        setImages(prev => [...prev, evt.target.files[0]])
                    }} id="fileupload" style={{ display: "none" }} type='file' accept='image/*'></input>
                    <Button style={{ margin: "8px" }} onClick={() => {
                        document.getElementById("fileupload")?.click()
                    }}>Subir imagen</Button>
                    <div style={{ display: "flex", flexWrap: "wrap", overflowY: "auto", flexBasis: 0, flexGrow: 1, justifyContent: "space-evenly", alignItems: "flex-start", alignContent: "flex-start" }}>
                        {images.map((el, index) => {
                            return (
                                <div onClick={() => {
                                    setImages(prev => {
                                        const newPrev = [...prev]
                                        newPrev.splice(index, 1)
                                        return newPrev
                                    })
                                }} style={{ borderRadius: "8px", overflow: "hidden", border: "1px solid lightgray", margin: "4px", width: "200px", height: "200px" }}>
                                    {typeof el === 'object' && 'path' in el
                                        ? <img style={{ objectFit: "cover", width: "100%", height: "100%" }} src={`http://localhost:8080/images/${el.path}`}></img>
                                        : <img style={{ objectFit: "cover", width: "100%", height: "100%" }} src={URL.createObjectURL(el)}></img>}

                                </div>)
                        }
                        )}
                    </div>
                </Card>
            </Col>
            <Col>
                <Card style={{ padding: "16px" }}>
                    <Form onSubmit={handleSubmit}>
                        <Form.Group controlId="nombre">
                            <Form.Label>Nombre del alojamiento</Form.Label>
                            <Form.Control
                                type="text"
                                name="nombre"
                                value={formData.nombre}
                                onChange={handleChange}
                                required
                            />
                        </Form.Group>

                        <Form.Group controlId="descripcion">
                            <Form.Label>Descripción</Form.Label>
                            <Form.Control
                                as="textarea"
                                rows={3}
                                name="descripcion"
                                value={formData.descripcion}
                                onChange={handleChange}
                                required
                            />
                        </Form.Group>

                        <Form.Group controlId="precio">
                            <Form.Label>Precio por noche</Form.Label>
                            <InputGroup>
                                <Form.Control
                                    type="number"
                                    name="precio"
                                    value={formData.precio}
                                    onChange={handleChange}
                                    required
                                />
                                <Form.Select
                                    name="moneda"
                                    value={formData.moneda}
                                    onChange={handleChange}
                                >
                                    {monedas.map((moneda) => (
                                        <option key={moneda} value={moneda}>{moneda}</option>
                                    ))}
                                </Form.Select>
                            </InputGroup>
                        </Form.Group>

                        <Row>
                            <Col>
                                <Form.Group controlId="checkin">
                                    <Form.Label>Horario Check-in</Form.Label>
                                    <Form.Control
                                        type="time"
                                        name="checkin"
                                        value={formData.checkin}
                                        onChange={handleChange}
                                        required
                                    />
                                </Form.Group>
                            </Col>
                            <Col>
                                <Form.Group controlId="checkout">
                                    <Form.Label>Horario Check-out</Form.Label>
                                    <Form.Control
                                        type="time"
                                        name="checkout"
                                        value={formData.checkout}
                                        onChange={handleChange}
                                        required
                                    />
                                </Form.Group>
                            </Col>
                        </Row>

                        <Form.Group controlId="huespedes">
                            <Form.Label>Número máximo de huéspedes</Form.Label>
                            <Form.Control
                                type="number"
                                name="huespedes"
                                value={formData.huespedes}
                                onChange={handleChange}
                                required
                            />
                        </Form.Group>

                        <Form.Group controlId="caracteristicas">
                            <Form.Label>Características</Form.Label>
                            <Select multiple value={caracteriticasSeleccionadas} onChange={(evt) => {
                                setCaracteristicasSeleccionadas(evt)
                            }} options={caracteristicasDisponibles.map(el => ({ label: el, value: el }))} />
                        </Form.Group>

                        <Row>
                            <Col sm={6}>
                                <Form.Group controlId="calle">
                                    <Form.Label>Calle</Form.Label>
                                    <Form.Control
                                        type="text"
                                        name="calle"
                                        value={formData.calle}
                                        onChange={handleChange}
                                        required
                                    />
                                </Form.Group>
                            </Col>
                            <Col sm={3}>
                                <Form.Group controlId="altura">
                                    <Form.Label>Altura</Form.Label>
                                    <Form.Control
                                        type="text"
                                        name="altura"
                                        value={formData.altura}
                                        onChange={handleChange}
                                        required
                                    />
                                </Form.Group>
                            </Col>
                            <Col sm={3}>
                                <Form.Group controlId="ciudad">
                                    <Form.Label>Ciudad</Form.Label>
                                    <Select value={ciudadSeleccionada} searchable options={ciudades.toSorted((a, b) => a.nombre < b.nombre ? -1 : 1).map(el => ({ label: el.nombre, value: el.id }))} onChange={(evt) => {
                                        setCiudadSeleccionada(evt)
                                    }} />
                                </Form.Group>
                            </Col>
                        </Row>

                        <Button className="mt-3" type="submit">
                            Guardar alojamiento
                        </Button>
                    </Form>
                </Card>
            </Col>
        </Row>
    </div>
}

function fileToBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result);
        reader.onerror = reject;
        reader.readAsDataURL(file);
    });
}