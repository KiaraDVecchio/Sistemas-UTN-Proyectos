export class BadRequestException extends Error {
  constructor(message) {
    super(message);
    this.name = "BadRequestException";
    this.status = 400;
  }
}