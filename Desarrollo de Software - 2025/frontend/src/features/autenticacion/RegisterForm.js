import { useState } from "react";
import { useAuth } from "../../context/Auth";
import { Button, Form, OverlayTrigger, Tooltip } from "react-bootstrap";
import { Loader } from "../../components/shared/Loader";
import { clickOnEnterKeyDown } from "../../utils/clickOnEnterKeyDown";

export const RegisterForm = ({ onHide }) => {

    const [loading, setLoading] = useState(false)

    const [values, setValues] = useState({
        name: '',
        email: '',
        password: '',
        confirmPassword: '',
        isAnfitrion: false
    })

    const { signUp } = useAuth()

    function updateValues(e) {
        const { name, value, checked } = e.target;

        if (name === 'isAnfitrion') {
            setValues(prev => ({
                ...prev,
                isAnfitrion: checked
            }));
        } else {
            setValues(prev => ({
                ...prev,
                [name]: value
            }));
        }
    }

    function handleSubmit(e) {
        setLoading(true)
        e.preventDefault()

        signUp(values, (res) => {
            if (res === true) {
                onHide()
            }
            setLoading(false)
        })
    }

    return <Form onSubmit={handleSubmit}>
        <Form.Group className="mb-3" controlId="name">
            <Form.Label>Nombre</Form.Label>
            <Form.Control
                type="text"
                name="name"
                placeholder="Ingresa tu nombre"
                value={values.name}
                onChange={updateValues}
                required
            />
        </Form.Group>
        <Form.Group className="mb-3" controlId="email">
            <Form.Label>Email</Form.Label>
            <Form.Control
                type="email"
                name="email"
                placeholder="Ingresa tu email"
                value={values.email}
                onChange={updateValues}
                required
            />
        </Form.Group>
        <Form.Group className="mb-3" controlId="password">
            <Form.Label>Contraseña</Form.Label>
            <Form.Control
                type="password"
                name="password"
                placeholder="Contraseña"
                value={values.password}
                onChange={updateValues}
                required
            />
        </Form.Group>
        <Form.Group className="mb-3" controlId="confirmPassword">
            <Form.Label>Confirmar contraseña</Form.Label>
            <Form.Control
                type="password"
                name="confirmPassword"
                placeholder="Confirma tu contraseña"
                value={values.confirmPassword}
                onChange={updateValues}
                required
            />
        </Form.Group>
        <Form.Group className="mb-3" controlId="isAnfitrion">
            <Form.Check
                type="checkbox"
                name="isAnfitrion"
                label={<span style={{ display: "inline-flex", alignItems: "center", justifyContent: "center", gap: "8px" }}>
                    Quiero ser anfitrión
                    <OverlayTrigger
                        trigger={["hover", "click"]}
                        placement="top"
                        delay={{ show: 250, hide: 250 }}
                        overlay={(props) => (
                            <Tooltip id="button-tooltip" {...props}>
                                Seleccione esta opción si desea publicar alojamientos para alquilar.
                            </Tooltip>
                        )}
                    >
                        <div tabIndex={0} onKeyDown={clickOnEnterKeyDown} onClick={(e) => e.preventDefault()} style={{ display: "flex", alignItems: "center", justifyContent: "center", borderRadius: "100%", border: "2px solid #555", width: '22px', height: '22px', padding: '2px' }}>
                            <svg xmlns="http://www.w3.org/2000/svg" xmlnsXlink="http://www.w3.org/1999/xlink" version="1.1" width="24" height="24" viewBox="0 0 256 256" xmlSpace="preserve">
                                <g style={{ stroke: 'none', strokeWidth: 0, strokeDasharray: 'none', strokeLinecap: 'butt', strokeLinejoin: 'miter', strokeMiterlimit: 10, fill: 'none', fillRule: 'nonzero', opacity: 1 }} transform="translate(1.4065934065934016 1.4065934065934016) scale(2.81 2.81)">
                                    <path d="M 49.083 71.489 l 5.776 -21.96 l 4.186 -15.247 c 3.497 -16.18 -32.704 -2.439 -38.002 1.695 l 0.425 4.853 c 4.824 -3.395 23.091 -7.744 19.449 4.275 l -1.634 6.135 l 0 0 l -8.329 31.071 c -3.497 16.18 32.704 2.439 38.002 -1.695 l -0.425 -4.853 C 63.708 79.159 45.441 83.508 49.083 71.489 z" style={{ stroke: 'none', strokeWidth: 1, strokeDasharray: 'none', strokeLinecap: 'butt', strokeLinejoin: 'miter', strokeMiterlimit: 10, fill: '#555', fillRule: 'nonzero', opacity: 1 }} transform=" matrix(1 0 0 1 0 0) " strokeLinecap="round" />
                                    <circle cx="53.871" cy="11.201" r="11.201" style={{ stroke: 'none', strokeWidth: 1, strokeDasharray: 'none', strokeLinecap: 'butt', strokeLinejoin: 'miter', strokeMiterlimit: 10, fill: '#555', fillRule: 'nonzero', opacity: 1 }} transform="  matrix(1 0 0 1 0 0) " />
                                </g>
                            </svg>
                        </div>
                    </OverlayTrigger></span>
                }
                checked={values.isAnfitrion}
                onChange={updateValues}
            />

        </Form.Group>
        <Button disabled={loading} variant="primary" type="submit" className="w-100 auth-submit-btn">
            {loading ? <Loader /> : "Registrate"}
        </Button>
    </Form>
}