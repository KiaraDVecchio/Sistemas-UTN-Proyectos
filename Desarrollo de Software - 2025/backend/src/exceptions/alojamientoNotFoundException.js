export class AlojamientoNotFoundException extends Error{
    constructor(){
        super()
        this.message = `No se encontraron coinicidencias.`
    }
}