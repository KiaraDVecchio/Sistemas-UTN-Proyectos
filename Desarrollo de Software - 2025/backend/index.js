import express from "express"
import { Server } from "./server.js";

import dotenv from "dotenv";
dotenv.config();
import routes from "./src/routes/routes.js"

import { AlojamientoRepository } from "./src/models/repositories/AlojamientoRepository.js";
import { AlojamientoService } from "./src/services/AlojamientoService.js";
import { AlojamientoController } from "./src/controllers/AlojamientoController.js";

import { NotificacionRepository } from "./src/models/repositories/NotificacionRepository.js";
import { NotificacionService } from "./src/services/NotificacionService.js";
import { NotificacionController } from "./src/controllers/NotificacionController.js";

import { ReservaRepository } from "./src/models/repositories/ReservaRepository.js";
import { ReservaService } from "./src/services/ReservaService.js";
import { ReservaController } from "./src/controllers/ReservaController.js";
import { MongoDBClient } from "./src/config/dbManager.js";
import { UsuarioController } from "./src/controllers/UsuarioController.js";
import { GeneralDataController } from "./src/controllers/GeneralDataController.js";
import { UsuariosRepository } from "./src/models/repositories/UsuarioRepo.js";
import { UsuarioService } from "./src/services/UsuarioService.js";
import { GeneralDataService } from "./src/services/GeneralDataService.js";
import { GeneralDataRepository } from "./src/models/repositories/GeneralDataRepo.js";

const app = express()
const port = Number.isNaN(Number(process.env.PORT)) ? 3000 : process.env.PORT

console.log("PORT: ", port)

await MongoDBClient.connect()

const server = new Server(app, port);


// Configuración de dependencias
const alojamientoRepo = new AlojamientoRepository();
const notificacionRepo = new NotificacionRepository();
const reservaRepo = new ReservaRepository();
const usuarioRepo = new UsuariosRepository();
const generalRepo = new GeneralDataRepository();

const alojamientoService = new AlojamientoService(alojamientoRepo, usuarioRepo, reservaRepo);
const notificacionService = new NotificacionService(notificacionRepo);
const reservaService = new ReservaService(reservaRepo, alojamientoRepo, notificacionRepo);
const usuarioService = new UsuarioService(usuarioRepo);
const generalDataService = new GeneralDataService(generalRepo);

const alojamientoController = new AlojamientoController(alojamientoService,);
const notificacionController = new NotificacionController(notificacionService);
const reservaController = new ReservaController(reservaService);
const usuarioController = new UsuarioController(usuarioService);
const generalDataController = new GeneralDataController(generalDataService);

// Registro del controlador en el servidor
server.setController(AlojamientoController, alojamientoController);
server.setController(NotificacionController, notificacionController);
server.setController(ReservaController, reservaController);
server.setController(UsuarioController, usuarioController);
server.setController(GeneralDataController, generalDataController);


// Configuración de rutas y lanzamiento
routes.forEach(r => {
    server.addRoute(r)
})
server.configureRoutes();
server.launch();

export { server };

// nodeCron("* * * *", async () => {
//     reservas = reservaRepo.findAll()
//     reservas.forEach(reserva => {
//         const fechaAValidar = new Date()
//         fechaAValidar.setDate(fechaAValidar.getDate() + 1)
//         if(reserva.fechaInicio.getTime() <  fechaAValidar.getTime()) {
//             Notifica.createNotificaion
//         }
//     })
// })


/*app.get("/healthcheck", (req, res) => {
    res.json({
        "status": "ok",
        "version": "1.0.0"
    })
})


app.listen(port, () => {
    console.log("El servidor arrancó correctamente en el puerto " + port)
})*/









