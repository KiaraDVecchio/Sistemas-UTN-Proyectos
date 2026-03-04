import path from "node:path"
import fs from "node:fs/promises" //este modulo trabaja con callbacks, al agregarle /promises trabaja con promesas
import { UsuarioModel } from "../schemas/usuarioSchema.js";

export class UsuariosRepository {
  async save(reserva) {
    const reservas = await this.findAll()
    reserva.id = reservas.length + 1
    reservas.push(reserva);
    const dataObjects = mapToDataObjects(reservas)
    await fs.writeFile(ReservaRepository.reservasPath, JSON.stringify(dataObjects))
    return reserva;
  }

  async findByEmail(email) {
    const model = await UsuarioModel.findOne({
      email
    })

    return model
  }

  async findById(id) {
    const model = await UsuarioModel.findById(id)

    return model
  }

  async create(usuario) {
    const model = UsuarioModel
    return await new model(usuario).save()
  }
}