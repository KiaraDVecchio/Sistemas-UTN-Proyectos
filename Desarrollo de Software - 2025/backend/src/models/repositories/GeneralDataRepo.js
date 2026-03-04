import { CiudadModel } from "../schemas/ciudadSchema.js";

export class GeneralDataRepository {
  async getLocalidadesByPais(pais) {
    const ciudades = await CiudadModel.find({
      pais
    })

    return ciudades.map(el => ({ id: el.id, nombre: el.nombre }))
  }
}