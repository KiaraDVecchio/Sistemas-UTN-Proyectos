export class ReservaIsCancelled extends Error {
    constructor(reservaId) {
        super()
        this.reservaId = reservaId
        this.message = `Cannot update a cancelled booking`
    }
}