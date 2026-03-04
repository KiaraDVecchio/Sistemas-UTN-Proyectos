import axios from "axios"

const availableFilters = [
    'ubicacion',
    'precioMinimo',
    'precioMaximo',
    'cantHuespedesPermitidos',
    'caracteristicasEspeciales',
    'page',
    'limit'
]


export async function getAlojamientosPaginated(filters, { signal }) {
    const params = {}

    Object.keys(filters).forEach(filter => {
        if (availableFilters.includes(filter)) {
            params[filter] = filters[filter]
        }
    });

    return axios.get("http://localhost:8080/alojamientos", {
        params,
        signal
    })
}