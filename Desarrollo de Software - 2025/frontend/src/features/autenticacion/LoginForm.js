import { Button, Form } from "react-bootstrap"
import { useAuth } from "../../context/Auth"
import { useState } from "react"
import { Loader } from "../../components/shared/Loader"

export const LoginForm = ({ onHide }) => {
    const [loading, setLoading] = useState(false)
    const [values, setValues] = useState({
        email: '',
        password: ''
    })

    const { login } = useAuth()

    function updateValues(e) {
        const { name, value, type, checked } = e.target;
        setValues(prev => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value
        }));
    }

    function handleSubmit(e) {
        e.preventDefault()
        login(values, (res) => {
            if (res === true) {
                onHide()
            }
            setLoading(false)
        })
    }

    return <Form onSubmit={handleSubmit}>
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
        <Button disabled={loading} variant="primary" type="submit" className="w-100 auth-submit-btn">
            {loading ? <Loader /> : "Iniciar sesión"}
        </Button>
    </Form>
}