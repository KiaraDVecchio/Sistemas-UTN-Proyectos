import { NavLink } from "react-router-dom"
import './admin-sidenav.css'

export const AdminSidebar = () => {
    return <nav className="admin__navbar-container">
        <NavLink
            className={({ isActive }) => isActive ? "admin__navlink active" : "admin__navlink"}
            to={'/me/admin/dashboard'}>Dashboard</NavLink>
        <NavLink
            className={({ isActive }) => isActive ? "admin__navlink active" : "admin__navlink"}
            to={'/me/admin/alojamientos'}>Mis alojamientos</NavLink>
        <NavLink
            className={({ isActive }) => isActive ? "admin__navlink active" : "admin__navlink"}
            to={'/me/admin/reservas'}>Reservas</NavLink>
    </nav>
}