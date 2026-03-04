import {
    Chart,
    LineController,
    LineElement,
    PointElement,
    LinearScale,
    Title,
    CategoryScale,
    Tooltip,
    Legend
} from 'chart.js'; import { useEffect, useRef } from "react"

Chart.register(LineController, LineElement, PointElement, LinearScale, Title, CategoryScale, Tooltip, Legend);


export const LineChartCustom = ({ data, alojamientosTotales }) => {

    const ref = useRef(null)

    useEffect(() => {
        let chart
        if (ref.current != null && data != null) {
            chart = new Chart(ref.current.getContext("2d"), {
                type: 'line',
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            ticks: {
                                stepSize: 1
                            },
                            beginAtZero: true
                        }
                    }
                },
                data: {
                    labels: data.map(row => row.fecha),
                    datasets: [
                        {
                            label: 'Reservas por día',
                            data: data.map(row => row.count),
                            borderColor: '#0d6efd',
                        }
                    ]
                },
            })
        }

        return () => {
            chart?.destroy()
        }
    }, [ref.current, data])

    return <div style={{ width: "100%", height: "100%", overflow: "auto", flexBasis: 0, flexShrink: 1, flexGrow: 1, display: "flex", justifyContent: "stretch" }} >
        <canvas ref={ref} style={{ width: "100%", height: "100%", flexGrow: 1 }}>

        </canvas>

    </div>
}