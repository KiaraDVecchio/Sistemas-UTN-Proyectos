import { AlojamientoController } from "../controllers/AlojamientoController.js";
import express, { urlencoded } from "express";
import { alojamientoExceptionMiddleware } from "../middlewares/alojamientoExceptionMiddleware.js";
import { validateAnfitrion, validateJwt } from "../middlewares/validateJwt.js";
import { uploadFotosMiddleware } from "../middlewares/uploadFotosMiddleware.js";

export default function alojamientoRoutes(getController) {
  const router = express.Router();

  router.get("/alojamientos", urlencoded({ extended: true }), async (req, res, next) => {
    try {
      await getController(AlojamientoController).findAll(req, res, next);
    } catch (error) {
      next(error);
    }
  });

  router.get("/alojamientos/me", validateJwt, validateAnfitrion, async (req, res, next) => {
    try {
      await getController(AlojamientoController).getOwn(req, res, next);
    } catch (error) {
      next(error);
    }
  });

  router.get("/alojamientos/:id", async (req, res, next) => {
    try {
      await getController(AlojamientoController).findById(req, res, next);
    } catch (error) {
      next(error);
    }
  });

  router.get("/alojamientos/:id/unavailable-dates", async (req, res, next) => {
    try {
      await getController(AlojamientoController).getAlreadyRentedDatesIntervals(req, res, next);
    } catch (error) {
      next(error);
    }
  });

  router.post('/alojamientos', validateJwt, validateAnfitrion, uploadFotosMiddleware, async (req, res, next) => {
    try {
      await getController(AlojamientoController).create(req, res, next);
    } catch (error) {
      next(error);
    }
  })

  router.post('/alojamientos/:id', validateJwt, validateAnfitrion, uploadFotosMiddleware, async (req, res, next) => {
    try {
      await getController(AlojamientoController).update(req, res, next);
    } catch (error) {
      next(error);
    }
  })

  router.use(alojamientoExceptionMiddleware);

  return router;
}
