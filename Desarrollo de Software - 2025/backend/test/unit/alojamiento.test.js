import { Alojamiento } from "../../src/models/entities/Alojamiento.js";
import {jest} from "@jest/globals"

describe('Alojamiento', () => {
    let alojamiento;

    beforeEach(() => {
        alojamiento = new Alojamiento(
            { nombre: 'Juan' },
            'Cabaña en el bosque',
            'Una cabaña',
            100,
            'USD',
            '14:00',
            '11:00',
            { calle: 'Falsa 123' },
            4,
            ['Wifi', 'Parra'],
            ['foto1.jpg']
        );
        alojamiento.reservas = [];
    });

    test('Retorna true si el precio está dentro del rango', () => {
        expect(alojamiento.tuPrecioEstaDentroDe(80, 120)).toBe(true);
    });

    test('Retorna false si el precio no está dentro del rango', () => {
        expect(alojamiento.tuPrecioEstaDentroDe(101, 200)).toBe(false);
    });

    test('Verifica si tiene una característica específica', () => {
        expect(alojamiento.tenesCaracteristica('Wifi')).toBe(true);
        expect(alojamiento.tenesCaracteristica('Piscina')).toBe(false);
    });

    test('Verifica si se pueden alojar cierta cantidad de huéspedes', () => {
        expect(alojamiento.puedenAlojarse(3)).toBe(true);
        expect(alojamiento.puedenAlojarse(5)).toBe(false);
    });

    test('Verifica disponibilidad cuando no hay reservas', () => {
        const rangoDeFechasMock = { inicio: '2025-06-01', fin: '2025-06-05' };
        expect(alojamiento.estasDisponibleEn(rangoDeFechasMock)).toBe(true);
    });

    test('Verifica disponibilidad cuando hay reservas, no se solapeen', () => {
        const reservaMock = {
            reservaValida: jest.fn().mockReturnValue(true)
        };
        alojamiento.reservas = [reservaMock];

        const rangoDeFechasMock = { inicio: '2025-06-01', fin: '2025-06-05' };
        expect(alojamiento.estasDisponibleEn(rangoDeFechasMock)).toBe(true);
        expect(reservaMock.reservaValida).toHaveBeenCalledWith(rangoDeFechasMock);
    });

    test('Verifica no disponibilidad cuando hay reservas que se solapan', () => {
        const reservaMock = {
            reservaValida: jest.fn().mockReturnValue(false)
        };
        alojamiento.reservas = [reservaMock];

        const rangoDeFechasMock = { inicio: '2025-06-01', fin: '2025-06-05' };
        expect(alojamiento.estasDisponibleEn(rangoDeFechasMock)).toBe(false);
        expect(reservaMock.reservaValida).toHaveBeenCalledWith(rangoDeFechasMock);
    });
});
