import { AlojamientoNotFoundException } from "../exceptions/alojamientoNotFoundException.js";
import { BadRequestException } from "../exceptions/badRequestException.js";

/**
 * Retorna el modelo de alojamiento según la conexión pasada como parámetro.
 * @param {import('mongoose').Connection} connection: Conexión a usar en el modelo. MongoDbClient.get<x>Connection();
 * @returns {import('mongoose').Model<Alojamiento>} modelo de mongose para ejecutar operaciones
 */
export class UsuarioController {
    constructor(usuarioService) {
        this.usuarioService = usuarioService;
    }

    async createUser(req, res) {
        const { nombre, email, tipo, contrasena, confirmarContrasena } = req.body
        if (contrasena !== confirmarContrasena) {
            res.status(400).send({ res: 'err', msg: "contraseñas diferentes" })
        } else {
            const { user, jwt } = await this.usuarioService.create({ nombre, email, contrasena, tipo })

            res.cookie('com.birbnb.auth', jwt, {
                httpOnly: false,
                secure: false,
                sameSite: 'strict',
                maxAge: 24 * 60 * 60 * 1000 * 15
            });

            res.send(user)
        }
    }

    async login(req, res) {
        const { email, contrasena } = req.body
        const { user, jwt } = await this.usuarioService.login(email, contrasena)
        if (user != null) {
            res.cookie('com.birbnb.auth', jwt, {
                httpOnly: false,
                secure: false,
                sameSite: 'strict',
                maxAge: 24 * 60 * 60 * 1000 * 15
            });

            return res.send(user)
        }
        res.status(500).send({ res: 'err', code: "UNEXPECTED_ERROR" })
    }

    async logout(_, res) {
        try {
            res.clearCookie('com.birbnb.auth')
            res.status(200).send()
        } catch (error) {
            res.status(500).send({ res: "ERROR", code: "UNEXPECTED_ERROR" })
        }
    }

    async getUser(req, res) {
        if (req.user?.id != null) {
            const user = await this.usuarioService.getUserById(req.user.id)
            if (user == null) {
                throw new Error("usuario no encontrado")
            }
            res.send(user)
        } else {
            res.status(403).send({ res: 'err', code: 'UNAUTHORIZED', msg: 'this endpoint is not available without being logged in' })
        }
    }

    async getAnfitrion(req, res) {
        const anfitrionId = req.params.anfitrionId

        const user = await this.usuarioService.getAnfitrionById(anfitrionId)
        if (user == null) {
            throw new Error("usuario no encontrado")
        }
        res.send(user)
    }


}
