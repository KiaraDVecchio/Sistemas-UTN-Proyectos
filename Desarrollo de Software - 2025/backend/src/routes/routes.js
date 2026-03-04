import alojamientoRoutes from "./AlojamientoRoutes.js"
import generalRoutes from "./GeneralRoutes.js";
import notificacionRoutes from "./NotificacionRoutes.js"
import reservaRoutes from "./ReservaRoutes.js"
import usuarioRoutes from "./UsuarioRoutes.js";
import { swaggerRoutes } from "./swaggerRoutes.js";

const routes = [
    alojamientoRoutes,
    notificacionRoutes,
    reservaRoutes,
    usuarioRoutes,
    generalRoutes,
    swaggerRoutes
]

export default routes;