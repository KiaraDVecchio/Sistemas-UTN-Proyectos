import "./App.css";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { HomePage } from "./features/home/HomePage";
import { AlojamientoDetailPage } from "./features/alojamiento/AlojamientoDetailPage";
import { Layout } from "./features/layout/Layout";
//import { RegisterPage } from "./features/Register";
//import { LoginPage } from "./features/Login";
import FormularioReserva from "./features/reserva/formularioReserva";
import { ReservasUsuario } from "./features/reserva/ReservasUsuario";
import { ReservaConfirmada } from './features/reserva/ReservaConfirmada';
import CheckoutPage from "./features/checkout/CheckoutPage";
import { AlojamientosPage } from "./features/me/AlojamientosPage";
import { EditAlojamientoPage } from "./features/me/AlojamientosPage/New";
import { useAuth } from "./context/Auth";
import { AdminLayout } from "./features/admin/pages/AdminLayout";
import { AdminReservasPage } from "./features/admin/pages/AdminReservas";
import { AdminDashboard } from "./features/admin/pages/AdminDashboard";

function App() {

  const { isAuthenticated } = useAuth()
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<HomePage />} />
        </Route>
        <Route path="/reserva" element={<Layout filters={false} />}>
          {/* <Route path="" element={<FormularioReserva />} /> */}
          <Route path=":id" element={<CheckoutPage />} />
        </Route>
        <Route path="/alojamientos" element={<Layout filters={false} />}>
          <Route path=":id" element={<AlojamientoDetailPage />} />
        </Route>
        {isAuthenticated && (<>
          <Route path="/me" element={<Layout filters={false} />}>
            <Route path="admin" element={<AdminLayout />}>
              <Route path="dashboard" element={<AdminDashboard />} />
              <Route path="reservas" element={<AdminReservasPage />} />
              <Route path="alojamientos" element={<AlojamientosPage />} />
              <Route path="alojamientos/new" element={<EditAlojamientoPage />} />
              <Route path="alojamientos/:id" element={<EditAlojamientoPage />} />
            </Route>
            <Route path="reservas" element={<ReservasUsuario />} />
          </Route>
          <Route path="/reserva-confirmada/:id" element={<ReservaConfirmada />} />
        </>
        )}
        <Route path="*" element={<Navigate to={"/"} />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;

//<Route path="register" element={<RegisterPage />} />
//<Route path="login" element={<LoginPage />} />
