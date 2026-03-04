import React from "react";
import ReactDOM from "react-dom/client";
import reportWebVitals from "./reportWebVitals";
import "bootstrap/dist/css/bootstrap.min.css";
import "./index.css";
import App from "./App";
import { AuthProvider } from './context/Auth';
import { ToastContextProvider } from "./context/Toast";
import { AlojamientosContextProvider } from "./context/Alojamientos";

console.log(`%c
   ___  _______  ___  _  _____ 
  / _ )/  _/ _ \\/ _ )/ |/ / _ )
 / _  |/ // , _/ _  /    / _  |
/____/___/_/|_/____/_/|_/____/ 
                               
`, "color: #322393ff;")

console.log("Proyecto con fines académicos. No es un sitio confiable para gestionar alojamientos y reservas.")

const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(
  <React.StrictMode>
    <ToastContextProvider>
      <AuthProvider>
        <AlojamientosContextProvider>
          <App />
        </AlojamientosContextProvider>
      </AuthProvider>
    </ToastContextProvider>
  </React.StrictMode>
);

reportWebVitals();
