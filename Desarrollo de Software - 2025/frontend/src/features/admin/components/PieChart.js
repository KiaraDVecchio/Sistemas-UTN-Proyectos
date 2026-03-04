import {
    Chart,
    DoughnutController,
    ArcElement,
    Tooltip,
    Legend
} from 'chart.js';
import { useEffect, useRef } from "react";

Chart.register(DoughnutController, ArcElement, Tooltip, Legend);

export const PieChartCustom = ({ data, alojamientosTotales }) => {
    const ref = useRef(null);

    useEffect(() => {
        let chart;
        if (ref.current != null && data != null) {
            chart = new Chart(ref.current.getContext("2d"), {
                type: 'doughnut', // Tipo ring
                data: {
                    labels: ['Tiene al menos una foto', 'Sin foto'],
                    datasets: [
                        {
                            label: 'Distribución',
                            data: [data.cumple, data.noCumple],
                            backgroundColor: ['#198754', '#dc3545'], // verde y rojo
                            borderWidth: 1
                        }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom'
                        },
                        tooltip: {
                            callbacks: {
                                label: function (context) {
                                    const value = context.raw;
                                    const label = context.label;
                                    return `${label}: ${(value / 100) * alojamientosTotales}`;
                                }
                            }
                        }
                    }
                }
            });
        }

        return () => {
            chart?.destroy();
        };
    }, [ref.current, data]);

    return (
        <div style={{ width: "100%", height: "100%", overflow: "auto", flexBasis: 0, flexShrink: 1, flexGrow: 1, display: "flex", justifyContent: "stretch" }} >
            <canvas ref={ref} style={{ width: "100%", height: "100%", flexGrow: 1 }}>

            </canvas>

        </div>
    );
};
