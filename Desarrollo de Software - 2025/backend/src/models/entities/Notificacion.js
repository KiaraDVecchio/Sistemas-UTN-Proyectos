export class Notificacion {
  mensaje;
  usuario;
  reservaAsociada;
  fechaAlta;
  leida;
  fechaLeida;
  id;

  constructor(mensaje, usuario, reservaAsociada) {
    this.mensaje = mensaje;
    this.usuario = usuario;
    this.reservaAsociada = reservaAsociada;
    this.fechaAlta = new Date();
    this.leida = false;
    this.fechaLeida = null;
  }

  marcarComoLeida() {
    if (!this.leida) {
      this.fechaLeida = new Date();
      this.leida = true;
    } else {
      throw new Error("La notificacion ya fue leida");
    }
  }
}