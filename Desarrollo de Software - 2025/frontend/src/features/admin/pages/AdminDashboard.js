import { useOutletContext } from 'react-router-dom'
import './admin-dashboard.css'
import DatePicker from '../../../components/DatePicker'
import { useMemo, useState } from 'react'
import { Button, Spinner } from 'react-bootstrap'
import { ReloadIcon } from '../../../components/shared/ReloadIcon'
import dayjs from 'dayjs'
import { LineChartCustom } from '../components/LineChart'
import { PieChartCustom } from '../components/PieChart'

window.dayjs = dayjs

export const AdminDashboard = () => {

    const { reservasState, alojamientosState, controllers: { getReservas, getAlojamientos } } = useOutletContext()

    const [dateFrom, setDateFrom] = useState(dayjs(new Date()).subtract(30, "days").toDate())
    const [dateTo, setDateTo] = useState(dayjs(new Date()).toDate())
    console.log(reservasState.data)
    const reservarFiltradas = useMemo(() => (reservasState.data?.filter(reserva => {
        const reservaDate = dayjs(reserva.fechaAlta?.split("T")[0])
        return (dateFrom == null || reservaDate.isAfter(dayjs(dateFrom))) && (dateTo != null || reservaDate.isBefore(dayjs(dateTo)))
    })), [dateFrom, dateTo, reservasState.data])

    console.log({ reservarFiltradas })

    const reservasUsd = reservarFiltradas?.filter(el => el.alojamientoSnapshot.moneda === "DOLAR_USA") ?? []
    const reservasArs = reservarFiltradas?.filter(el => el.alojamientoSnapshot.moneda === "PESO_ARG") ?? []
    const reservasReal = reservarFiltradas?.filter(el => el.alojamientoSnapshot.moneda === "REAL") ?? []

    const reservasTotales = reservarFiltradas?.filter(reserva => reserva.estado === "CONFIRMADA").length

    const reservasPorDiaData = []
    const fechas = getDatesBetween(dateFrom, dateTo)
    fechas.forEach((fecha) => {
        reservasPorDiaData.push({ fecha: fecha.format("DD/MM/YYYY"), count: reservarFiltradas?.filter(reserva => dayjs(reserva.fechaAlta).isSame(fecha, "day"))?.length ?? 0 })
    })

    console.log({ reservasPorDiaData })

    const alojamientosConFoto = alojamientosState.data != null ? (alojamientosState.data?.filter(el => el.fotos[0] != null).length / alojamientosState.data?.length * 100) : null
    const aljamientosFotosData = alojamientosConFoto != null ? { cumple: alojamientosConFoto, noCumple: 100 - alojamientosConFoto } : null

    return (
        <div class="admin-dashboard-container">
            <div class="div1 card p-2" style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
                <span className='fw-bold'>Creado desde:</span>
                <DatePicker value={dateFrom} setValue={setDateFrom} />
            </div>
            <div class="div2 card p-2" style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
                <span className='fw-bold'>Creada hasta:</span>
                <DatePicker value={dateTo} setValue={setDateTo} />
            </div>
            <div class="div3 card p-2">
                <Button onClick={() => { getReservas(); getAlojamientos() }} style={{ display: "flex", alignItems: "center", justifyContent: "center", height: "100%", flexDirection: "column" }}>
                    {(reservasState.loading || alojamientosState.loading) ? <Spinner /> : <ReloadIcon />}
                </Button>
            </div>
            <div class="div4 card p-2" style={{ display: "flex", flexDirection: "column" }}>
                <span className='fw-bold'>Historial de reservas recibidas por día</span>
                <LineChartCustom data={reservasPorDiaData} />
            </div>
            <div class="div5 card p-2" style={{ display: "flex", flexDirection: "column" }}>
                <span className='fw-bold'>Alojamientos con foto</span>
                {(aljamientosFotosData == null || alojamientosState.data == null) ? (
                    <Spinner />
                ) : (
                    <PieChartCustom
                        data={aljamientosFotosData}
                        alojamientosTotales={alojamientosState.data.length}
                    />
                )}

            </div >
            <div class="div6 card p-2">
                <span className='fw-bold'>Ticket Promedio (por moneda)</span>
                <ul className='fs-4 p-0 m-auto' style={{ textAlign: "center" }}>
                    <li style={{ listStyle: "none", marginBottom: "8px" }}>USD {(reservasUsd.length === 0 ? 0 : reservasUsd.reduce((prev, curr) => (prev + curr.precioPorNoche * Number(dayjs(curr.rangoFechas.fechaFin).diff(dayjs(curr.rangoFechas.fechaInicio), "days"))), 0) / reservasUsd.length).toFixed(2)}</li>
                    <li style={{ listStyle: "none", marginBottom: "8px" }}>ARS {(reservasArs.length === 0 ? 0 : reservasArs.reduce((prev, curr) => (prev + curr.precioPorNoche * Number(dayjs(curr.rangoFechas.fechaFin).diff(dayjs(curr.rangoFechas.fechaInicio), "days"))), 0) / reservasArs.length).toFixed(2)}</li>
                    <li style={{ listStyle: "none" }}>REAL {(reservasReal.length === 0 ? 0 : reservasReal.reduce((prev, curr) => (prev + curr.precioPorNoche * Number(dayjs(curr.rangoFechas.fechaFin).diff(dayjs(curr.rangoFechas.fechaInicio), "days"))), 0) / reservasReal.length).toFixed(2)}</li>
                </ul>
            </div>
            <div class="div7 card p-2">
                <p className='m-0'><span className='fw-bold'>Reservas pendientes totales</span> (sin filtrar por fecha de creación)</p>
                <span className='fs-1 m-auto p-0'>{reservasState?.data?.filter(reserva => reserva.estado === "PENDIENTE").length}</span>
            </div>
            <div class="div8 card p-2">
                <span className='fw-bold'>Reservas canceladas totales</span>
                <span className='fs-1 m-auto p-0'>{reservarFiltradas?.filter(reserva => reserva.estado === "CANCELADA").length}</span>
            </div>
            <div class="div9 card p-2" style={{ display: "flex", flexDirection: "column" }}>
                <span className='fw-bold'>Reservas confirmadas (total: {reservasTotales})</span>
                <div style={{ width: "auto", height: "100%", overflow: "auto", flexBasis: 0, flexShrink: 1, flexGrow: 1 }}>
                    <table className='table' >
                        <thead>
                            <tr>
                                <th>Alojamiento</th>
                                <th>Reservas</th>
                            </tr>
                        </thead>
                        <tbody>
                            {alojamientosState.data?.map(el => ({
                                Element: () => <tr key={el._id}>
                                    <td>
                                        {el.nombre}
                                    </td>
                                    <td>
                                        {reservarFiltradas?.filter(reserva => reserva.alojamiento === el.id && reserva.estado === "CONFIRMADA").length}
                                    </td>
                                </tr>,
                                reservas: reservarFiltradas?.filter(reserva => reserva.alojamiento === el.id && reserva.estado === "CONFIRMADA").length
                            })).toSorted((a, b) => a.reservas < b.reservas ? 1 : -1).map(el => <el.Element />)}
                        </tbody>
                    </table>
                </div>
            </div>
        </div >)
}

function getDatesBetween(fechaDesde, fechaHasta) {
    const dates = []

    let current = dayjs(fechaDesde)
    const end = dayjs(fechaHasta)

    while (current.isSame(end, 'day') || current.isBefore(end, 'day')) {
        dates.push(current);
        current = current.add(1, 'day');
    }

    return dates;
}