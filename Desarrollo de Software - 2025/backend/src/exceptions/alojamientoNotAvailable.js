export class AlojamientoNotAvailableError extends Error {
    constructor(alojamiento) {
        super()
        this.message = `Alojamiento con nombre ${alojamiento.nombre} not available` 
    }
}