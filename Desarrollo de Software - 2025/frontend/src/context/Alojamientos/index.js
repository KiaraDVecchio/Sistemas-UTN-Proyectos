import { createContext, useContext, useState } from "react";

// Los alojamientos los guardamos en un array donde cada posición del array es una página.
// Cada posición tiene estado de loading, error y su información

export const AlojamientosContext = createContext({
    alojamientos: [],
    totalPages: 1,
    startFetchingAlojamientos: () => { },
    receiveAlojamientos: () => { },
    setAlojamientosError: () => { },
    getAlojamientoStateByPage: () => { },
    resetAlojamientos: () => { }
})

export const AlojamientosContextProvider = ({ children }) => {

    const [totalPages, setTotalPages] = useState(1)
    const [state, setState] = useState([])
    const [filters, setFilters] = useState({})

    function startFetchingAlojamientos(page) {
        setState((prev) => {
            const newPrev = JSON.parse(JSON.stringify(prev))
            newPrev[page] = { data: null, error: null, loading: true }
            return newPrev
        })
    }

    function receiveAlojamientos(data, page, totalPages = 1) {
        setTotalPages(totalPages)
        setState((prev) => {
            const newPrev = JSON.parse(JSON.stringify(prev))
            newPrev[page] = { data: data, error: null, loading: false }
            return newPrev
        })
    }

    function setAlojamientosError(page) {
        setState((prev) => {
            const newPrev = JSON.parse(JSON.stringify(prev))
            newPrev[page] = { data: null, error: true, loading: false }
            return newPrev
        })
    }

    function resetAlojamientos() {
        setState([])
    }

    function getAlojamientoStateByPage(page) {
        return state[page]
    }

    return <AlojamientosContext.Provider value={{
        alojamientos: state,
        startFetchingAlojamientos,
        receiveAlojamientos,
        setAlojamientosError,
        getAlojamientoStateByPage,
        resetAlojamientos,
        totalPages,
        filters, setFilters
    }}>
        {children}
    </AlojamientosContext.Provider>
}

export function useAlojamientos() {
    return useContext(AlojamientosContext)
}

export function useAlojamientosTotalPages() {
    const { totalPages } = useContext(AlojamientosContext)
    return totalPages
}

/**
 * Retorna estado y dispatch action de los filtros de los alojamientos
 * @returns {[{[key: string]: string}, import("react").Dispatch<import("react").SetStateAction<{[key: string]: string}>>]} modelo de mongose para ejecutar operaciones
 */
export function useAlojamientosFilters() {
    const { filters, setFilters } = useContext(AlojamientosContext)
    return [filters, setFilters]
}