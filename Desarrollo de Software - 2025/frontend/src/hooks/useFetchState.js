import { useReducer } from "react"

function reducer(state, action) {
    switch (action.type) {
        case 'START_LOADING':
            return {
                loading: true,
                error: false,
                data: null
            }
            break;
        case 'RECEIVE_DATA':
            return {
                loading: false,
                error: false,
                data: action.payload
            }
            break;
        case 'START_ERROR':
            return {
                loading: false,
                error: true,
                data: null
            }
            break;
    }
    return state
}

/**
 * Custom hook: Manejo de estados básico típico de una request.
 * @returns { {
 *     state: {
 *       loading: boolean,
 *       error: boolean,
 *       data: any
 *     },
 *     actions: {
 *       startLoading: () => void,
 *       receiveData: (payload: any) => void,
 *       startError: () => void,
 *     }
 *   } } Estado y actions para modificar el mismo.
 */
export function useFetchState() {
    const [state, dispatch] = useReducer(reducer, { loading: false, error: null, data: null })

    function startLoading() {
        dispatch({ type: 'START_LOADING' })
    }
    function receiveData(payload) {
        dispatch({ type: 'RECEIVE_DATA', payload })
    }
    function startError() {
        dispatch({ type: 'START_ERROR' })
    }

    return { state, actions: { startError, startLoading, receiveData } }
}
