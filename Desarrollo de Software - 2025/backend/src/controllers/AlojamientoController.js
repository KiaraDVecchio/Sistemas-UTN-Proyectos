import { AlojamientoNotFoundException } from "../exceptions/alojamientoNotFoundException.js";
import { BadRequestException } from "../exceptions/badRequestException.js";

export class AlojamientoController {
  constructor(alojamietoService) {
    this.alojamientoService = alojamietoService;
  }

  async create(req, res, next) {
    try {
      const alojamiento = await this.alojamientoService.create({ ...req.body, fotos: req.files }, req.user.id)
      res.send(alojamiento)

    } catch (error) {
      next(error)
    }
  }

  async update(req, res, next) {
    try {
      const alojamiento = await this.alojamientoService.update({ ...req.body, fotos: req.files, id: req.params.id }, req.user.id)
      res.send(alojamiento)

    } catch (error) {
      next(error)
    }
  }

  async findAll(req, res, next) {
    try {
      const {
        ubicacion,
        precioMinimo,
        precioMaximo,
        cantHuespedesPermitidos,
        caracteristicasEspeciales,
        page = "1",
        limit = "10",
      } = req.query;

      // Validaciones básicas
      if (precioMinimo !== undefined && isNaN(Number(precioMinimo))) {
        throw new BadRequestException("precioMinimo debe ser un número.");
      }

      // Construcción de objeto filtros
      const filtros = {
        ubicacion,
        precioMinimo: precioMinimo ? Number(precioMinimo) : null,
        precioMaximo: precioMaximo ? Number(precioMaximo) : null,
        cantHuespedesPermitidos: cantHuespedesPermitidos
          ? Number(cantHuespedesPermitidos)
          : null,
        caracteristicasEspeciales: caracteristicasEspeciales
          ? caracteristicasEspeciales.split(",").map((c) => c.trim())
          : [],
        page: Number(page),
        limit: Number(limit),
      };

      const alojamientos = await this.alojamientoService.findAll(filtros);

      //si no hay datos, lanzo Not Found
      if (alojamientos.data.length === 0) {
        res.status(204)
      }
      res.json(alojamientos);
    } catch (error) {
      next(error);
    }
  }

  async getOwn(req, res, next) {
    try {
      const alojamientos = await this.alojamientoService.getByUser(req.user.id)
      res.send({ data: alojamientos })
    } catch (error) {
      next(error)
    }
  }

  async findById(req, res, next) {
    try {
      const id = req.params.id;
      const alojamiento = await this.alojamientoService.findById(id);
      if (!alojamiento) {
        throw new AlojamientoNotFoundException(id);
      }
      res.json(alojamiento);
    } catch (error) {
      next(error);
    }
  }

  async getAlreadyRentedDatesIntervals(req, res, next) {
    const id = req.params.id;
    if (!id) {
      return res.status(400).send({ res: "err", code: "MISSING_PARAMS" })
    }
    const dates = await this.alojamientoService.getUnavailableDates(id)

    res.send({ res: "ok", data: dates })
  }
}
