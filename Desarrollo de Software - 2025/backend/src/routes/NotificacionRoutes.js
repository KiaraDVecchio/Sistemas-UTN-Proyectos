import { NotificacionController } from "../controllers/NotificacionController.js";
import express from "express";
import { notificacionExceptionMiddleware } from "../middlewares/notificacionExceptionMiddleware.js";
import { validateJwt } from "../middlewares/validateJwt.js";

export default function notificacionRoutes(getController) {
  const router = express.Router();

  router.get("/notificaciones", validateJwt, async (req, res, next) => {
    try {
      await getController(NotificacionController).findAll(req, res, next);
    } catch (error) {
      next(error);
    }
  });

  router.get("/notificaciones/:id", validateJwt, async (req, res, next) => {
    try {
      await getController(NotificacionController).findById(req, res, next);
    } catch (error) {
      next(error);
    }
  });
  router.patch("/notificaciones/:id", validateJwt, async (req, res, next) => {
    try {
      await getController(NotificacionController).update(req, res, next);
    } catch (error) {
      next(error);
    }
  });


  // todo: eliminar post (las notificaciones se crean con la actualizacion de reservas o similar)
  router.post("/notificaciones", async (req, res, next) => {
    try {
      await getController(NotificacionController).create(req, res, next);
    } catch (error) {
      next(error);
    }
  });

  router.use(notificacionExceptionMiddleware);

  return router;
}
