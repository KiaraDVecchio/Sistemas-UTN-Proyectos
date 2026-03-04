export class RangoFechas {
    constructor(fechaInicio, fechaFin) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }


    seSuperponeCon(otroRango) {
        return (
            this.fechaInicio <= otroRango.fechaFin
            && this.fechaFin >= otroRango.fechaInicio
        );
    }

}