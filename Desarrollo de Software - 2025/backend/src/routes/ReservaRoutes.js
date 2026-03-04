import express from 'express';
import { ReservaController } from '../controllers/ReservaController.js';
import { reservasErrorHandler } from '../middlewares/reservasMiddleware.js';
import { validateAnfitrion, validateHuesped, validateJwt } from '../middlewares/validateJwt.js';

/**
 * Rutas para manejo de reservas.
 *  - GET /reservas           -> listado paginado, opcional ?user=ID
 *  - GET /reservas/:id       -> detalle por ID
 *  - POST /reservas          -> crear reserva
 *  - PUT /reservas/:id       -> actualizar reserva
 *  - DELETE /reservas/:id    -> eliminar/cancelar reserva
 */
export default function reservaRoutes(getController) {
  const router = express.Router();

  // retornamos solo las reservas del usuario. Sería un problema de seguridad si retornamos todas las reservas y filtramos por query param unicamente
  router.get('/reservas', validateJwt, async (req, res, next) => {
    try {
      await getController(ReservaController).findAll(req, res);
    } catch (err) {
      next(err);
    }
  });

  router.get('/reservas/admin', validateJwt, validateAnfitrion, async (req, res, next) => {
    try {
      await getController(ReservaController).findByAnfitrion(req, res);
    } catch (err) {
      next(err);
    }
  })

  // Detalle de una reserva por su ID
  router.get('/reservas/:id', validateJwt, async (req, res, next) => {
    try {
      await getController(ReservaController).findById(req, res);
    } catch (err) {
      next(err);
    }
  });

  // Crear una nueva reserva
  router.post('/reservas', validateJwt, async (req, res, next) => {
    try {
      await getController(ReservaController).create(req, res);
    } catch (err) {
      next(err);
    }
  });

  // Confirmar reserva
  router.put('/reservas/:id/confirm', validateJwt, validateAnfitrion, async (req, res, next) => {
    try {
      await getController(ReservaController).confirm(req, res);
    } catch (err) {
      next(err);
    }
  });

  // Actualizar fechas de una reserva existente
  router.put('/reservas/:id', validateJwt, async (req, res, next) => {
    try {
      await getController(ReservaController).update(req, res);
    } catch (err) {
      next(err);
    }
  });

  // Cancelar/eliminar una reserva
  router.delete('/reservas/:id', validateJwt, async (req, res, next) => {
    try {
      await getController(ReservaController).delete(req, res);
    } catch (err) {
      next(err);
    }
  });

  router.use(reservasErrorHandler)  //va a funcionar como el next para todas las rutas definidas arriba

  return router;
}


//Flujo: llega la request a nuestra ruta, entra al try y si sale mal se atrapa al error y se manda al next (midelware excepciones), en donde siempre se manda una respuesta, sea generica o definida por nosotros