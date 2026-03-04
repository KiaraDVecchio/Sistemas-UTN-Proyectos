import path from "node:path";
import { Alojamiento } from "../entities/Alojamiento.js";
import { modelNames } from "../schemas/constants.js";
import mongoose from "mongoose";
import { AlojamientoModel } from "../schemas/alojamientoSchema.js";
import * as _ from "../schemas/ciudadSchema.js";
import * as __ from "../schemas/paisSchema.js";

export class AlojamientoRepository {
  static alojamientosPath = path.join("data", "alojamientos.json");

  async findAll() {
    const alojamientos = await AlojamientoModel.find()
    return alojamientos;
  }

  async save(alojamiento) {
    if (alojamiento.id) {
      // actualizacion
    } else {
      const model = AlojamientoModel
      const _alojamiento = await new model(alojamiento).save()
      return _alojamiento
    }
  }

  async findByPage(pageNum, limitNum) {
    // const offset = (pageNum - 1) * limitNum;
    const alojamientos = await AlojamientoModel
      .find()
      .populate({
        path: 'direccion',
        populate: {
          path: 'ciudad',
          populate: 'pais'
        },
      })
      .sort({ createdAt: 1 })
      // .skip(offset)
      // .limit(limitNum)
      .exec();
    return alojamientos
  }

  async countAll() {
    const alojamientos = await this.findAll();
    return alojamientos.length;
  }

  async findById(id) {
    const alojamiento = await AlojamientoModel.findById(id)
    return alojamiento;
  }

  async findByIdPopulateReservas(id) {
    const alojamiento = await AlojamientoModel.findById(id).populate('reservas')
    return alojamiento;
  }

  async findByAnfitrion(userId) {
    const alojamientos = await AlojamientoModel.find({
      anfitrion: userId
    }).populate('reservas')
    return alojamientos;
  }
}

function mapToAlojamientos(dataObjects) {
  return dataObjects.map(mapToAlojamiento);
}

function mapToAlojamiento(dataObject) {
  const {
    anfitrion,
    nombre,
    descripcion,
    precioPorNoche,
    moneda,
    horarioCheckIn,
    horarioCheckOut,
    direccion,
    cantHuespedesMax,
    caracteristicas,
    fotos,
  } = dataObject;
  const alojamiento = new Alojamiento(
    anfitrion,
    nombre,
    descripcion,
    precioPorNoche,
    moneda,
    horarioCheckIn,
    horarioCheckOut,
    direccion,
    cantHuespedesMax,
    caracteristicas,
    fotos
  );
  alojamiento.id = dataObject.id;
  return alojamiento;
}

function mapToDataObject(alojamiento) {
  const {
    anfitrion,
    nombre,
    descripcion,
    precioPorNoche,
    moneda,
    horarioCheckIn,
    horarioCheckOut,
    direccion,
    cantHuespedesMax,
    caracteristicas,
    fotos,
  } = alojamiento;
  return {
    id: alojamiento.id,
    nombre,
    descripcion,
    precioPorNoche,
    moneda,
    horarioCheckIn,
    horarioCheckOut,
    direccion,
    cantHuespedesMax,
    caracteristicas,
    fotos,
  };
}

function mapToDataObjects(alojamientos) {
  return alojamientos.map(mapToDataObject);
}
