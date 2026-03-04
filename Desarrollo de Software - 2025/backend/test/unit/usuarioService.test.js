import { UsuarioService } from '../../src/services/UsuarioService.js';
import { AlreadyExistsUser, LoginUserException, NotFoundUserException } from '../../src/exceptions/userExceptions.js';
import { TipoUsuario } from '../../src/models/enums/TipoUsuario.js';
import { Usuario } from '../../src/models/entities/Usuario.js';
import {jest} from "@jest/globals"
import bcrypt from "bcrypt";

bcrypt.genSaltSync = jest.fn();
bcrypt.compareSync = jest.fn();
bcrypt.hashSync = jest.fn();


describe('UsuarioService', () => {
    let mockUserRepo;
    let usuarioService;

    beforeEach(() => {
        mockUserRepo = {
            findByEmail: jest.fn(),
            create: jest.fn(),
            findById: jest.fn(),
        };
        usuarioService = new UsuarioService(mockUserRepo);
    });

    describe('login', () => {
        it('Deberia loguear correctamente ', async () => {
            const user = new Usuario('Valentino', 'test@gmail.com', TipoUsuario.HUESPED, 'holaaa');
            user._id = '123';

            mockUserRepo.findByEmail.mockResolvedValue(user);
            bcrypt.compareSync.mockReturnValue(true);

            const resultado = await usuarioService.login('test@gmail.com', 'password123');

            expect(typeof resultado.jwt).toBe('string');
            expect(resultado.user).toEqual({
                nombre: 'Valentino',
                email: 'test@gmail.com',
                tipo: TipoUsuario.HUESPED,
                id: '123'
            });
        })

        it('Deberia lanzar excepción si el usuario no existe', async () => {
            mockUserRepo.findByEmail.mockResolvedValue(null);

            await expect(usuarioService.login('no@test.com', 'test'))
                .rejects
                    .toThrow(LoginUserException);
        });

        it('Deberia lanzar excepción si la contraseña es incorrecta', async () => {
            const user = new Usuario('Valentino', 'test@example.com', TipoUsuario.HUESPED, 'test');
            mockUserRepo.findByEmail.mockResolvedValue(user);
            bcrypt.compareSync.mockReturnValue(false);

            await expect(usuarioService.login('test@gmail.com', 'passIncorrecta'))
                .rejects
                .toThrow(LoginUserException);
        });
    });

    describe('create', () => {
        it('Deberia crear un nuevo usuario correctamente', async () => {
            mockUserRepo.findByEmail.mockResolvedValue(null);
            mockUserRepo.create.mockImplementation((user) => {
                user._id = '123';
                return user;
            });

            bcrypt.genSaltSync.mockReturnValue('salt');
            bcrypt.hashSync.mockReturnValue('contraHasehada');

            const resultado = await usuarioService.create({
                nombre: 'Nuevo Usuario',
                email: 'nuevo@example.com',
                contrasena: 'secreta'
            });

            expect(resultado.user).toEqual({
                nombre: 'Nuevo Usuario',
                email: 'nuevo@example.com',
                tipo: TipoUsuario.HUESPED,
                id: '123'
            });
            expect(typeof resultado.jwt).toBe('string');
        });

        it('Deberia lanzar excepción si el email ya existe', async () => {
            mockUserRepo.findByEmail.mockResolvedValue({ email: 'existe@gmail.com' });

            await expect(usuarioService.create({
                nombre: 'Ya existe',
                email: 'existe@gmail.com',
                contrasena: '1234'
            })).rejects.toThrow(AlreadyExistsUser);
        });
    });

    describe('getUserById', () => {
        it('Deberia devolver el DTO del usuario si existe', async () => {
            const fakeUser = new Usuario('Valentino', 'valentino@gmail.com', TipoUsuario.HUESPED, 'pass');
            fakeUser._id = 'abc123';
            mockUserRepo.findById.mockResolvedValue(fakeUser);

            const result = await usuarioService.getUserById('abc123');

            expect(result).toEqual({
                nombre: 'Valentino',
                email: 'valentino@gmail.com',
                tipo: TipoUsuario.HUESPED,
                id: 'abc123'
            });
        });

        it('Debería lanzar excepción si el usuario no se encuentra', async () => {
            mockUserRepo.findById.mockResolvedValue(null);

            await expect(usuarioService.getUserById('inexistente'))
                .rejects
                .toThrow(NotFoundUserException);
        })
    });
});
