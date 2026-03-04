import express from "express";
import { GeneralDataController } from "../controllers/GeneralDataController.js";

export default function generalRoutes(getController) {
    const router = express.Router();

    router.get("/generaldata/paises/:paisId/localidades", async (req, res, next) => {
        try {
            await getController(GeneralDataController).getLocalidadesByPais(req, res, next);
        } catch (error) {
            next(error);
        }
    });

    // router.use(userExceptionMiddleware);

    return router;
}
