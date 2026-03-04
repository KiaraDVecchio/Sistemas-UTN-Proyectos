import path from "node:path"
import { Reserva } from "../entities/Reserva.js";
import { Alojamiento } from "../entities/Alojamiento.js";
import { RangoFechas } from "../entities/RangoFechas.js";
import { ReservaNotFoundError } from "../../exceptions/reservaNotFound.js";
import { AlojamientoModel } from "../schemas/alojamientoSchema.js";
import { ReservaModel } from "../schemas/reservaSchema.js";
export class ReservaRepository {

  static reservasPath = path.join("data", "reservas.json"); //eliminé el constructor porque como ahora vamos a trabajar con archivos, no necesitamos el array.

  async findAll() {
    const reserva = await ReservaModel.find()
    return reserva;
  }

  async findByUser(id) {
    const reserva = await ReservaModel.find({
      huespedReservador: id
    })
    return reserva;
  }

  async findById(id) {
    const reserva = await ReservaModel.findById(id)
    if (!reserva) { throw new ReservaNotFoundError(id) }
    return reserva;
  }

  async findByAlojamientoId(alojamientoId) {
    const reservas = await ReservaModel.find({
      alojamiento: alojamientoId
    })

    return reservas
  }

  async findByIdPopulatedAlojamiento(id) {
    const reserva = await ReservaModel.findById(id).populate({
      path: 'alojamiento',
      populate: {
        path: 'reservas',
      },
    })
    if (!reserva) { throw new ReservaNotFoundError(id) }
    return reserva;
  }

  async findByPage(pageNum, limitNum, userId) {
    const offset = (pageNum - 1) * limitNum;
    const reservas = await ReservaModel
      .find({
        huespedReservador: userId
      })
      .sort({ createdAt: 1 })
      .skip(offset)
      .limit(limitNum)
      .exec();
    return reservas
  }

  async save(reserva) {
    if (reserva.id) {
      await reserva.save()
    } else {
      const model = ReservaModel
      const _reserva = await new model(reserva).save()
      return _reserva
    }
  }

  async deleteById(id) { //ESTE METODO YA NO SIRVE
    // yo acá haría una baja lógica, no eliminaría directamente en el repsoitorio.
    // Le pondría una fecha de finalización o un flag que indique que la reserva está cancelada
    const index = this.reservas.findIndex(r => r.id === id);
    if (index === -1) return false;
    this.reservas.splice(index, 1);
    return true;
  }

  async countAll() {
    const reservas = await this.findAll()
    return reservas.length;
  }
}

function mapToReserva(data) {
  // 1) Reconstruyo el Alojamiento con su lista de reservas vacía por ahora
  const aloj = new Alojamiento(
    data.alojamiento.anfitrion,
    data.alojamiento.nombre,
    data.alojamiento.descripcion,
    data.alojamiento.precioPorNoche,
    data.alojamiento.moneda,
    data.alojamiento.horarioCheckIn,
    data.alojamiento.horarioCheckOut,
    data.alojamiento.direccion,
    data.alojamiento.cantHuespedesMax,
    data.alojamiento.caracteristicas,
    data.alojamiento.fotos
  );

  // 2) Reconstruyo el rango de fechas
  //    Asegúrate que el JSON use las mismas claves (en tu ejemplo: "rango": { inicio, fin })
  const rango = new RangoFechas(
    new Date(data.rango.inicio),
    new Date(data.rango.fin)
  );

  // 3) Creo la Reserva con las entidades ya válidas
  const reserva = new Reserva(data.huesped, aloj, rango);
  reserva.id = data.id;            // setter en tu clase Reserva
  reserva.estado = data.estado;    // opcional, si guardas estado en JSON
  reserva.fechaAlta = new Date(data.fechaAlta);
  // si tu JSON incluye registroDeCambiosDeEstado, podrías reasignarlo también

  return reserva;
}

function mapToReservas(dataArray) {
  return dataArray.map(mapToReserva);
}

// Y para persistir, tu mapToDataObject no necesita tocar Alojamiento ni RangoFechas, 
// solo vuelves a plano las propiedades:
function mapToDataObject(reserva) {
  return {
    id: reserva.id,
    huesped: reserva.huesped,
    alojamiento: {
      anfitrion: reserva.alojamiento.anfitrion,
      nombre: reserva.alojamiento.nombre,
      descripcion: reserva.alojamiento.descripcion,
      precioPorNoche: reserva.alojamiento.precioPorNoche,
      moneda: reserva.alojamiento.moneda,
      horarioCheckIn: reserva.alojamiento.horarioCheckIn,
      horarioCheckOut: reserva.alojamiento.horarioCheckOut,
      direccion: reserva.alojamiento.direccion,
      cantHuespedesMax: reserva.alojamiento.cantHuespedesMax,
      caracteristicas: reserva.alojamiento.caracteristicas,
      fotos: reserva.alojamiento.fotos
    },
    rango: {
      inicio: reserva.rangoFechas.fechaInicio.toISOString(),
      fin: reserva.rangoFechas.fechaFin.toISOString()
    },
    estado: reserva.estado
    // opcional: estado, fechaAlta, registroDeCambiosDeEstado…
  };
}

function mapToDataObjects(reservas) {
  return reservas.map(mapToDataObject);
}