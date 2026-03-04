import { Outlet } from "react-router-dom"
import { AdminSidebar } from "../components/Sidebar"
import { Col, Row } from "react-bootstrap"
import { useFetchState } from "../../../hooks/useFetchState"
import axios from "axios"
import { useEffect } from "react"
import { getMyAlojamientos } from "../../me/AlojamientosPage/api/getMyAlojamientos"

export const AdminLayout = () => {

    const {
        actions: reservasActions, state: reservasState
    } = useFetchState()

    const {
        actions: alojamientosActions, state: alojamientosState
    } = useFetchState()

    function getReservas() {
        reservasActions.startLoading()
        axios.get("http://localhost:8080/reservas/admin?page=2", { withCredentials: true }).then(res => {
            if (res.status === 200) {
                reservasActions.receiveData(res.data)
            } else {
                reservasActions.startError()
            }
        })
    }

    function getAlojamientos() {
        alojamientosActions.startLoading()
        getMyAlojamientos().then((res) => {
            alojamientosActions.receiveData(res.data)
        }).catch(() => {
            alojamientosActions.startError()
        })
    }

    useEffect(() => {
        getReservas()
    }, [])

    useEffect(() => {
        if (alojamientosState.data == null) {
            getAlojamientos()
        }
    }, [])

    return (
        <Row className="w-100" style={{ minHeight: 'calc(100vh - 130px)' }}>
            <Col xs="auto" style={{ position: "sticky", top: 0 }}>
                <AdminSidebar />
            </Col>
            <Col>
                <div style={{ padding: "32px", height: '100%' }}>
                    <Outlet context={{ reservasState, reservasActions, alojamientosActions, alojamientosState, controllers: { getReservas, getAlojamientos } }} />
                </div>
            </Col>
        </Row>
    )
}