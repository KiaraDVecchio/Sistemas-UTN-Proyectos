import { Alojamiento } from "../models/entities/Alojamiento.js";
import { Direccion } from "../models/entities/Direccion.js";
import { Foto } from "../models/entities/Foto.js";
import { deleteStaticFile } from "../utils/deleteStaticFile.js";

export class AlojamientoService {
  constructor(alojamientoRepository, usuarioRepo, reservaRepo) {
    this.alojamientoRepository = alojamientoRepository;
    this.usuarioRepository = usuarioRepo;
    this.reservaRepository = reservaRepo;
  }

  async create(alojamiento, userId) {
    const fotos = alojamiento.fotos.map(foto => new Foto(foto.originalname, foto.filename))
    if (alojamiento.direccion == null || alojamiento.direccion === '') {
      throw new Error("Direccion inválida")
    }
    const direccion = new Direccion(JSON.parse(alojamiento.direccion))
    const caracteristicas = alojamiento.caracteristicas == null || alojamiento.caracteristicas === '' ? [] : JSON.parse(alojamiento.caracteristicas)
    const _alojamiento = await this.alojamientoRepository
      .save(new Alojamiento(
        userId,
        alojamiento.nombre,
        alojamiento.descripcion,
        alojamiento.precioPorNoche,
        alojamiento.moneda,
        alojamiento.horarioCheckIn,
        alojamiento.horarioCheckOut,
        direccion,
        alojamiento.cantHuespedesMax,
        caracteristicas,
        fotos))
    return this.toDto(_alojamiento)
  }

  async update(alojamiento, userId) {

    const _alojamiento = await this.alojamientoRepository.findById(alojamiento.id)

    if (!_alojamiento.anfitrion.equals(userId)) {
      throw new Error("INVALID OPERATION")
    }
    const newFotos = alojamiento.fotos.map(foto => new Foto(foto.originalname, foto.filename))
    const keepPhotos = _alojamiento.fotos.filter(foto => alojamiento.keep?.includes(foto.path)).map(el => new Foto(el.description, el.path))
    const deletePhotos = _alojamiento.fotos.filter(foto => !alojamiento.keep?.includes(foto.path))
    if (alojamiento.direccion == null || alojamiento.direccion === '') {
      throw new Error("Direccion inválida")
    }
    const direccion = new Direccion(JSON.parse(alojamiento.direccion))
    const caracteristicas = alojamiento.caracteristicas == null || alojamiento.caracteristicas === '' ? [] : JSON.parse(alojamiento.caracteristicas)

    _alojamiento.nombre = alojamiento.nombre;
    _alojamiento.descripcion = alojamiento.descripcion;
    _alojamiento.precioPorNoche = alojamiento.precioPorNoche;
    _alojamiento.moneda = alojamiento.moneda;
    _alojamiento.horarioCheckIn = alojamiento.horarioCheckIn;
    _alojamiento.horarioCheckOut = alojamiento.horarioCheckOut;
    _alojamiento.direccion = direccion;
    _alojamiento.cantHuespedesMax = alojamiento.cantHuespedesMax;
    _alojamiento.caracteristicas = caracteristicas;
    _alojamiento.fotos = [...newFotos, ...keepPhotos];

    _alojamiento.save()

    deletePhotos.forEach(foto => {
      deleteStaticFile(foto.path)
    });
    return this.toDto(_alojamiento)
  }

  async getByUser(userId) {
    const alojamientos = await this.alojamientoRepository.findByAnfitrion(userId)
    return alojamientos.map((el) => this.toDto(el))
  }

  async findAll({
    ubicacion,
    precioMinimo,
    precioMaximo,
    cantHuespedesPermitidos,
    caracteristicasEspeciales,
    page = 1,
    limit = 10,
  }) {
    const pageNum = Math.max(Number(page), 1);
    const limitNum = Math.min(Math.max(Number(limit), 1), 100);

    let alojamientos = await this.alojamientoRepository.findByPage(
      // pageNum,
      // limitNum
    );

    // 1) Filtrar por ubicación (ciudad o país, case-insensitive)
    if (ubicacion) {
      const loc = ubicacion.toLowerCase();
      alojamientos = alojamientos.filter(
        (a) =>
          a.direccion.ciudad?.nombre?.toLowerCase().includes(loc) ||
          a.direccion.ciudad?.pais?.nombre?.toLowerCase().includes(loc)
      );
    }

    // Filtro de precio
    if (precioMinimo != null) {
      alojamientos = alojamientos.filter((a) =>
        a.tuPrecioEsMayorA(precioMinimo)
      );
    }
    if (precioMaximo != null) {
      alojamientos = alojamientos.filter((a) =>
        a.tuPrecioEsMenorA(precioMaximo)
      );
    }

    // Filtro de características
    if (
      caracteristicasEspeciales.length > 0
    ) {
      alojamientos = alojamientos.filter((a) =>
        caracteristicasEspeciales.every((c) => a.tenesCaracteristica(c))
      );
    }

    // Filtro de capacidad de huéspedes
    if (cantHuespedesPermitidos != null) {
      alojamientos = alojamientos.filter((a) =>
        a.puedenAlojarse(cantHuespedesPermitidos)
      );
    }

    const total = alojamientos?.length ?? 0;
    const total_pages = Math.ceil(total / limitNum);

    const offset = (pageNum - 1) * limitNum;
    alojamientos = (alojamientos ?? []).slice(offset, offset + limitNum)


    return {
      page: pageNum,
      per_page: limitNum,
      total,
      total_pages,
      data: alojamientos.map((a) => this.toDto(a)),
    };
  }

  async getUnavailableDates(id) {
    const reservas = await this.reservaRepository.findByAlojamientoId(id)

    if (Array.isArray(reservas)) {
      return reservas.map(el => [el.rangoFechas.fechaInicio, el.rangoFechas.fechaFin])
    } else {
      return []
    }
  }

  toDto(alojamiento, anfitrion = null) {
    if (anfitrion) {
      return {
        anfitrion: anfitrion,
        nombre: alojamiento.nombre,
        descripcion: alojamiento.descripcion,
        precioPorNoche: alojamiento.precioPorNoche,
        moneda: alojamiento.moneda,
        checkin: alojamiento.horarioCheckIn,
        checkout: alojamiento.horarioCheckOut,
        cantHuespedesMax: alojamiento.cantHuespedesMax,
        caracteristicas: alojamiento.caracteristicas,
        direccion: alojamiento.direccion,
        fotos: alojamiento.fotos,
        id: alojamiento._id,
      };
    }
    return {
      nombre: alojamiento.nombre,
      precioPorNoche: alojamiento.precioPorNoche,
      cantHuespedesMax: alojamiento.cantHuespedesMax,
      caracteristicas: alojamiento.caracteristicas,
      direccion: alojamiento.direccion,
      fotos: alojamiento.fotos,
      id: alojamiento._id,
      moneda: alojamiento.moneda
    };
  }

  async findById(id) {
    const alojamiento = await this.alojamientoRepository.findById(id);
    const anfitrionData = await this.usuarioRepository.findById(alojamiento.anfitrion)

    const { nombre, createdAt } = anfitrionData
    return alojamiento ? this.toDto(alojamiento, { nombre, createdAt }) : null;
  }
}
