import jwt from "jsonwebtoken"
import bcrypt from 'bcrypt'
import { Usuario } from "../models/entities/Usuario.js";
import { AlreadyExistsUser, LoginUserException, NotFoundUserException } from "../exceptions/userExceptions.js";
import { TipoUsuario } from "../models/enums/TipoUsuario.js";

const { sign } = jwt

export class UsuarioService {
    constructor(usuarioRepository) {
        this.usersRepo = usuarioRepository;
    }

    async login(email, password) {
        const user = await this.usersRepo.findByEmail(email)
        if (user == null) {
            throw new LoginUserException()
        }
        const passIsCorrect = bcrypt.compareSync(password, user.contrasena)
        if (!passIsCorrect) {
            throw new LoginUserException()
        }
        const jwt = await this.generateJwt(user)
        return { user: this.toDto(user), jwt }
    }

    async create({ nombre, email, contrasena, tipo = TipoUsuario.HUESPED }) {
        const exists = await this.usersRepo.findByEmail(email) != null

        if (exists) {
            throw new AlreadyExistsUser(email)
        }
        const salt = bcrypt.genSaltSync()
        const pass = bcrypt.hashSync(contrasena, salt)

        const newUser = await this.usersRepo.create(new Usuario(nombre, email, tipo, pass))
        const jwt = await this.generateJwt(newUser)
        return { user: this.toDto(newUser), jwt }
    }

    async getUserById(userId) {
        const user = await this.usersRepo.findById(userId)
        if (user == null) {
            throw new NotFoundUserException()
        }
        return this.toDto(user)
    }

    async getAnfitrionById(userId) {
        const user = await this.usersRepo.findById(userId)
        if (user == null || user.tipo !== 'ANFITRION') {
            throw new NotFoundUserException()
        }
        return this.toDto(user)
    }

    /**
     * Retorna DTO de usuario.
     * @param {Usuario} user: instancia de User (modelo de dominio);
     * @returns {{nombre: string, id: string, email: string}} modelo de mongose para ejecutar operaciones
     */
    toDto(user) {
        return {
            nombre: user.nombre,
            email: user.email,
            tipo: user.tipo,
            id: user._id
        }
    }

    generateJwt(user) {
        return new Promise((resolve, reject) => {
            const payload = { id: user._id, tipo: user.tipo }
            sign(payload, process.env.JWT_SIGNATURE ?? 'development', {
                expiresIn: "15d"
            }, (err, token) => {
                if (err) {
                    console.error(err)
                    reject("No se pudo generar el token")
                } else {
                    resolve(token)
                }
            })
        })
    }
}