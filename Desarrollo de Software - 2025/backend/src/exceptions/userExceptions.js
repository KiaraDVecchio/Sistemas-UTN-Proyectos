export class AlreadyExistsUser extends Error {
    constructor(userEmail) {
        super()
        this.userEmail = userEmail
        this.message = `Cannot create user with email: "${userEmail}"`
    }
}

export class NotFoundUserException extends Error {
    constructor() {
        super()
        this.status = 404
        this.message = `Cannot find user`
    }
}

export class LoginUserException extends Error {
    constructor() {
        super()
        this.status = 400
        this.message = `Cannot authenticate`
    }
}