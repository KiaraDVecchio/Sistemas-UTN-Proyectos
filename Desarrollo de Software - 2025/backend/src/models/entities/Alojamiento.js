export class Alojamiento {
  constructor(anfitrion, nombre, descripcion, precioPorNoche, moneda, horarioCheckIn, horarioCheckOut, direccion, cantHuespedesMax, caracteristicas, fotos) {
    this.anfitrion = anfitrion;
    this.nombre = nombre;
    this.descripcion = descripcion;
    this.precioPorNoche = precioPorNoche;
    this.moneda = moneda;
    this.horarioCheckIn = horarioCheckIn;
    this.horarioCheckOut = horarioCheckOut;
    this.direccion = direccion;
    this.cantHuespedesMax = cantHuespedesMax;
    this.caracteristicas = caracteristicas;
    this.fotos = fotos;
    // this.reservas = [];
  }

  esDeAnfitrion(anfitrionId) {
    return this.anfitrion.equals(anfitrionId)
  }

  estasDisponibleEn(rangoDeFechas) {
    return this.reservas.every(
      (reserva) => (reserva.reservaValida(rangoDeFechas))
    );
  }

  tuPrecioEstaDentroDe(valorMinimo, valorMaximo) {
    return this.precioPorNoche >= valorMinimo && this.precioPorNoche <= valorMaximo
  }

  tuPrecioEsMenorA(valor) {
    return this.precioPorNoche <= valor
  }
  tuPrecioEsMayorA(valor) {
    return this.precioPorNoche >= valor
  }

  tenesCaracteristica(caracteristica) {
    return this.caracteristicas.includes(caracteristica);
  }

  puedenAlojarse(cantHuespedes) {
    return cantHuespedes <= this.cantHuespedesMax
  }

  getCantHuespedesMax() {
    return this.cantHuespedesMax
  }

  getNombre() {
    return this.nombre
  }

  getPrecioPorNoche() {
    return this.precioPorNoche
  }

  /*agregarReserva(rangoFechas, cantidadHuespedes, huespedReservador) {
    
    if (huespedReservador.tipo !== TipoUsuario.HUESPED) {
      throw new Error("Solo los huéspedes pueden hacer reservas");
    }

    if (!this.puedenAlojarse(cantidadHuespedes)) {
      throw new Error(`Este alojamiento admite ${this.getCantHuespedesMax()} como máximo. Usted solicitó ${cantidadHuespedes} huespedes`);
    }
    
    if (!this.estasDisponibleEn(rangoFechas)) {
      throw new Error("Este alojamiento está ocupado en las fechas seleccionadas");
    }

    const reserva = new Reserva(
      huespedReservador,
      this,
      rangoFechas
    );

    this.reservas.push(reserva);
    return reserva;
  }*/

}

