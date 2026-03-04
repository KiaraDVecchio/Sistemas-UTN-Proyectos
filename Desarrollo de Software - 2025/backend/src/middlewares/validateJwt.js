import jwt from "jsonwebtoken"
import { TipoUsuario } from "../models/enums/TipoUsuario.js"

const { verify } = jwt

export function validateJwt(req, res, next) {
    try {
        const token = req.headers.authorization?.split(" ")[1] ?? req.cookies['com.birbnb.auth']
        const payload = verify(token, process.env.JWT_SIGNATURE ?? 'development')
        if (payload.id != null) {
            req.user = payload
            next()
        }
    } catch (error) {
        res.status(403).send({ res: "ERROR", code: "UNAUTHORIZED" })
    }
}

export function validateAnfitrion(req, res, next) {
    if (req.user.tipo === TipoUsuario.ANFITRION) {
        next()
    } else {
        res.status(403).send({ res: "ERROR", code: "UNAUTHORIZED" })
    }
}

export function validateHuesped(req, res, next) {
    if (req.user.tipo === TipoUsuario.HUESPED) {
        next()
    } else {
        res.status(403).send({ res: "ERROR", code: "UNAUTHORIZED" })
    }
}