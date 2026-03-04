import React, { useEffect, useState } from "react"
import { getMyAlojamientos } from "./api/getMyAlojamientos"
import { Button, Spinner } from "react-bootstrap"
import { AlojamientoCard } from "./AlojamientoCard"
import { useNavigate, useOutletContext } from "react-router-dom"

export const AlojamientosPage = () => {

    const { alojamientosState: state } = useOutletContext()

    const navigate = useNavigate()

    return (
        <>
            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "32px" }}>
                <h1>Mis alojamientos</h1>
                <Button onClick={() => {
                    navigate("/me/admin/alojamientos/new")
                }}>Agregar nuevo</Button>
            </div>
            <div style={{ minHeight: "80vh", display: "flex", alignItems: "center", justifyContent: "space-between", flexWrap: "wrap", gap: "16px", rowGap: "16px" }}>
                {state.loading && <Spinner style={{ margin: "auto" }} />}
                {state.data?.map(alojamiento => <AlojamientoCard key={alojamiento.id} alojamiento={alojamiento} />)}
            </div>
        </>)
}