import { createContext, useContext, useState } from 'react'
import { Toast } from 'react-bootstrap'

const ToastContext = createContext({
    addToast: () => { }
})

// interface toastI {
//     content: string,
//     title: string,
//     type?: "danger" | "info" | "warning" | "default",
//     closable?: boolean
//     durationMs?: number
// }

export const ToastContextProvider = ({ children }) => {
    const [currConter, setCurrentCounter] = useState(0)
    const [toasts, setToasts] = useState([])

    function addToast(toastData) {
        const id = currConter
        setToasts(prev => [...prev, { ...toastData, id }])
        setCurrentCounter(prev => prev + 1)
        setTimeout(() => {
            setToasts(prev => prev.filter(el => el.id !== id))
        }, toastData.durationMs ?? 5000)
    }

    function removeToast(id) {
        setToasts(prev => prev.filter(_el => _el.id !== id))
    }

    return (
        <ToastContext.Provider value={{ addToast }}>
            {children}
            <div style={{ zIndex: 99999, position: "fixed", display: "flex", flexDirection: "column", right: "16px", top: "124px", gap: "8px" }}>
                {/* <ToastContainer position="top-end" className="p-3" style={{ zIndex: 1 }}> */}
                {toasts.map(el => (
                    <Toast
                        onClose={() => { removeToast(el.id) }}
                        className="d-inline-block m-1"
                        bg={(el.type ?? "primary").toLowerCase()}
                        key={el.id}
                    >
                        {el.title && <Toast.Header>
                            {el.title}
                        </Toast.Header>}
                        <Toast.Body className='text-white'>
                            {el.content}
                        </Toast.Body>
                    </Toast>
                ))}
                {/* </ToastContainer> */}
            </div>
        </ToastContext.Provider>
    )
}


/**
 * Retorna una función para agregar toasts.
 * @returns {function({
 * content: string,
 * title: string,
 * durationMs?: number,
 * type: "danger" | "warning" | "success" | "info" | "primary" | "secondary"
 * }): void} modelo de mongose para ejecutar operaciones
 */
export function useToast() {
    const { addToast } = useContext(ToastContext)
    return addToast
}
