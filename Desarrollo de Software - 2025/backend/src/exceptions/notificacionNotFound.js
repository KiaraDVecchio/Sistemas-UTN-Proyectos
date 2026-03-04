export class NotificacionNotFoundError extends Error{
    constructor(){
        super()
        this.message = `No se encontraron coincidencias.`
    }
}