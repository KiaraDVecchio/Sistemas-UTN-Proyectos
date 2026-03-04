import { join } from 'path';
import dotenv from 'dotenv';
dotenv.config({ path: join(process.cwd(), '/.test.env') });
import { jest } from "@jest/globals"
import mongoose from 'mongoose';
import { ReservaModel } from '../../src/models/schemas/reservaSchema.js';



const TEST_USER_ID = '685226d2e7ca1224326dd0bd';
const TEST_ALOJAMIENTO_ID = '685260ebe0971a3dc7ba4793';
const TEST_ALOJAMIENTO_ID2 = '685260ebe0971a3dc7ba4790';

jest.unstable_mockModule('../../src/middlewares/validateJwt.js', () => ({
  validateJwt: (req, res, next) => {
    req.user = { id: '685226d2e7ca1224326dd0bd', tipo: 'HUESPED' };
    next();
  },
  validateHuesped: (req, res, next) => {
    next();
  },
  validateAnfitrion: (req, res, next) => {
    next();
  }
}));

let request, server;

beforeAll(async () => {
  const supertest = await import('supertest');
  const appModule = await import('../../index.js');

  request = supertest.default;
  server = appModule.server;

  if (mongoose.connection.readyState === 0) {
    
    const uri = `${process.env.MONGODB_URI}/${process.env.MONGODB_NAME}?authSource=admin`
    
    await mongoose.connect(uri)
   
  }
});

afterEach(async () => {
  await ReservaModel.deleteMany({});    //borro las reservas creadas en el test despues del mismo.
});



describe('POST /reservas - Crear una reserva', () => {
  it('debería crear una reserva y devolver 200 con los datos correctos', async () => {
    const response = await request(server.app)
      .post('/reservas')
      .send({
        alojamiento: TEST_ALOJAMIENTO_ID,
        rangoFechas: {
          desde: '2025-07-10',
          hasta: '2025-07-12'
        },
        huespedes: '2'
      })
      .expect(200);

    expect(response.body).toMatchObject({
      alojamiento: {
        _id: TEST_ALOJAMIENTO_ID,
        nombre: expect.any(String),
        precioPorNoche: expect.any(Number),

      },
      huesped: TEST_USER_ID,
      huespedes: 2,
      estado: expect.any(String),
      rangoFechas: {
        fechaInicio: '2025-07-10T00:00:00.000Z',
        fechaFin: '2025-07-12T00:00:00.000Z'
      },
      id: expect.any(String),
    });

    const reservaEnMongo = await ReservaModel.findById(response.body.id);


    expect(reservaEnMongo).toBeDefined();
    expect(reservaEnMongo.huespedReservador.toString()).toBe(TEST_USER_ID);
    expect(reservaEnMongo.alojamiento.toString()).toBe(TEST_ALOJAMIENTO_ID);
    expect(reservaEnMongo.estado).toBe("PENDIENTE");
    expect(reservaEnMongo.huespedes).toBe(2);
  });

  it('debería devolver 400 si la cantidad de huéspedes no es válida', async () => {
    const response = await request(server.app)
      .post('/reservas')
      .send({
        alojamiento: TEST_ALOJAMIENTO_ID,
        rangoFechas: {
          desde: '2025-07-10',
          hasta: '2025-07-12'
        },
        huespedes: 'abc'
      });

    expect(response.status).toBe(400);
    expect(response.body).toHaveProperty('error', 'Huéspedes inválidos');
  });
  it('debería devolver 400 si el alojamiento ya está reservado en ese rango de fechas', async () => {
    await request(server.app)
      .post('/reservas')
      .send({
        alojamiento: TEST_ALOJAMIENTO_ID2,
        rangoFechas: {
          desde: '2025-07-20',
          hasta: '2025-07-22'
        },
        huespedes: '2'
      })
      .expect(200);

    const response = await request(server.app)
      .post('/reservas')
      .send({
        alojamiento: TEST_ALOJAMIENTO_ID2,
        rangoFechas: {
          desde: '2025-07-21',
          hasta: '2025-07-23'
        },
        huespedes: '2'
      });

    expect(response.status).toBe(400);
  });

  afterAll(async () => {
    await mongoose.disconnect();
  });


});