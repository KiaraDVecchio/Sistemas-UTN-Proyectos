import express from "express";
import { userExceptionMiddleware } from "../middlewares/userExceptionMiddleware.js";
import { UsuarioController } from "../controllers/UsuarioController.js";
import { validateJwt } from "../middlewares/validateJwt.js";

export default function usuarioRoutes(getController) {
    const router = express.Router();

    router.post("/users", async (req, res, next) => {
        try {
            await getController(UsuarioController).createUser(req, res, next);
        } catch (error) {
            next(error);
        }
    });

    router.get('/users', validateJwt, async (req, res, next) => {
        try {
            await getController(UsuarioController).getUser(req, res, next);
        } catch (error) {
            next(error);
        }
    })

    router.get('/users/anfitrion/:anfitrionId', async (req, res, next) => {
        try {
            await getController(UsuarioController).getAnfitrion(req, res, next);
        } catch (error) {
            next(error);
        }
    })

    router.post('/users/login', async (req, res, next) => {
        try {
            await getController(UsuarioController).login(req, res, next);
        } catch (error) {
            next(error);
        }
    })

    router.post('/users/logout', async (req, res, next) => {
        try {
            await getController(UsuarioController).logout(req, res, next);
        } catch (error) {
            next(error);
        }
    })

    router.use(userExceptionMiddleware);

    return router;
}
