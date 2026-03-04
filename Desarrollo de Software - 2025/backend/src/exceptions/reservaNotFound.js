export class ReservaNotFoundError extends Error {
    constructor(reservaId) {
        super()
        this.reservaId = reservaId
        this.message = `Reserva with ID ${this.reservaId} not found`
    }
}