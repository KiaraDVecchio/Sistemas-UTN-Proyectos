import { Reserva } from "../../src/models/entities/Reserva.js";
import { EstadoReserva } from "../../src/models/enums/EstadoReserva.js";
import { FactoryNotificacion } from "../../src/models/entities/FactoryNotificacion.js";
import { RangoFechas } from "../../src/models/entities/RangoFechas.js";
import { jest } from "@jest/globals";
import { CambioEstadoReserva } from "../../src/models/entities/CambioEstadoReserva.js";

describe('Reserva', () => {
    let huesped;
    let alojamiento;
    let rangoFechas;
    let reserva;

    beforeEach(() => {
        FactoryNotificacion.crearSegunReserva = jest.fn();
        huesped = { nombre: "Carlos" };
        alojamiento = {
            getPrecioPorNoche: () => 150,
        };
        rangoFechas = { fechaInicio: "2025-06-01", fechaFin: "2025-06-05" };
        reserva = new Reserva(huesped, alojamiento, rangoFechas);
    });

    test('Actualiza el estado y registra el cambio, ademas notifica',
        () => {
            const cambioEstadoMock = new CambioEstadoReserva(new Date(), EstadoReserva.CONFIRMADA, 1)
            reserva.actualizarEstado(cambioEstadoMock);

            expect(reserva.estado).toBe(EstadoReserva.CONFIRMADA);
            expect(reserva.registroDeCambiosDeEstado).toContain(cambioEstadoMock);
            // expect(FactoryNotificacion.crearSegunReserva).toHaveBeenCalledWith(reserva);
            // lo elimino porque esto se hace en el service al final (no sé si está bien)
        });

    describe('reservaValida', () => {
        test('Retorna true si el estado no es confirmada', () => {
            reserva.estado = EstadoReserva.PENDIENTE;
            const nuevoRango = new RangoFechas("2025-06-10", "2025-06-15");
            expect(reserva.reservaValida(nuevoRango)).toBe(true);
        });

        test('Retorna true si no se superpone aunque esté confirmada', () => {
            reserva.estado = EstadoReserva.CONFIRMADA;
            const nuevoRango = new RangoFechas("2025-06-10", "2025-06-15");
            reserva.rangoFechas = new RangoFechas("2025-06-01", "2025-06-05");
            expect(reserva.reservaValida(nuevoRango)).toBe(true);
        });

        test('Retorna false si está confirmada y se superpone', () => {
            reserva.estado = EstadoReserva.CONFIRMADA;
            reserva.rangoFechas = new RangoFechas("2025-06-01", "2025-06-10");
            const nuevoRango = new RangoFechas("2025-06-05", "2025-06-12");
            expect(reserva.reservaValida(nuevoRango)).toBe(false);
        });
    });
});
