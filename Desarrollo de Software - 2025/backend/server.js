import cors from "cors"
import cookieParser from "cookie-parser";
import express from "express";
import path from "path"

export class Server {
  #controllers = {};
  #routes = [];
  #app;

  constructor(app, port = 3000) {
    this.#app = app;
    this.port = port;
    this.#app.use(cors({
      origin: 'http://localhost:3000',
      credentials: true
    }));
    this.#app.get("/healthcheck", (req, res) => {
      res.json({
        "status": "ok",
        "version": "1.0.1"
      })
    })
    this.#app.use(express.json());
    this.#app.use('/images', express.static(path.join(process.cwd(), 'uploads')));
    this.#app.use(cookieParser());
  }

  get app() {
    return this.#app;
  }

  setController(controllerClass, controller) {
    this.#controllers[controllerClass.name] = controller;
  }

  getController(controllerClass) {
    const controller = this.#controllers[controllerClass.name];
    if (!controller) {
      throw new Error("Controller missing for the given route.");
    }
    return controller;
  }

  configureRoutes() {
    this.#routes.forEach(route => {
      this.app.use(route(this.getController.bind(this)))
    })
  }


  launch() {
    this.app.listen(this.port, () => {
      console.log("Server running on port " + this.port);
    });
  }

  addRoute(route) {
    this.#routes.push(route)
  }
}