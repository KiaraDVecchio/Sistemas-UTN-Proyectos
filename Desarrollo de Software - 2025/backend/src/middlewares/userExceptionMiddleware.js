import { BadRequestException } from "../exceptions/badRequestException.js";
import { AlreadyExistsUser, LoginUserException, NotFoundUserException } from "../exceptions/userExceptions.js";

export function userExceptionMiddleware(err, req, res, next) {
  if (err.constructor.name == AlreadyExistsUser.name) {
    return res.status(500).json({ error: err.message });
  }
  if (err.constructor.name == NotFoundUserException.name) {
    return res.status(err.status).json({ res: 'err', code: 'NOT_FOUND', msg: err.message });
  }
  if (err.constructor.name == LoginUserException.name) {
    return res.status(err.status).json({ res: 'err', code: 'AUTH_ERROR', msg: err.message });
  }
  if (err.constructor.name == BadRequestException.name) {
    return res.status(err.status).json({ error: err.message });
  }
  console.error(err)
  return res.status(500).json({ res: 'err', code: 'UNKNOWN', msg: 'Ocurrió un error inesperado' });
}
