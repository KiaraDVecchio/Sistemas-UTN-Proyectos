import Footer from "../../components/footer/Footer";
import { Outlet } from "react-router";
import Header from "../../components/header/Header";
import './layout.css'
import { MenuAccesible } from "../../components/MenuAccesible";

export function Layout({ filters = true }) {
  return (
    <>
      <button onClick={() => {
        document.getElementById("menu_accesibility").querySelector('[role="menuitem"]').focus()
      }} id="skip-link" className="skip-link">Ir al menú de accesibilidad</button>
      <Header filters={filters} />
      <Outlet></Outlet>
      <Footer></Footer>
      <MenuAccesible />
    </>
  )
}
