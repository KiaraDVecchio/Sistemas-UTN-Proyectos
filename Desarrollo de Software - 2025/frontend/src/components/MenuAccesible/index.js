import { useEffect, useState } from "react"
import { Button } from "react-bootstrap"

export const MenuAccesible = ({
    items = []
}) => {

    const [currentIndex, setCurrentIndex] = useState(-1)

    const [menu, setMenu] = useState(null)
    const _items = menu != null ? [...menu.querySelectorAll('[role="menuitem"]')] : []

    useEffect(() => {
        if (menu != null) {
            window.addEventListener('keydown', (e) => {
                if (menu.contains(e.target)) {
                    if (e.key === 'ArrowDown') {
                        e.preventDefault();
                        setCurrentIndex(prev => (prev + 1) % _items.length)
                    } else if (e.key === 'ArrowUp') {
                        e.preventDefault();
                        setCurrentIndex(prev => (prev - 1) % _items.length)
                    } else if (e.key === 'Escape') {
                        e.preventDefault();
                        setCurrentIndex(-1)
                        document.getElementById("skip-link")?.focus();
                    }
                }
            });
        }
    }, [menu])

    useEffect(() => {
        if (currentIndex !== -1) {
            _items[currentIndex]?.focus();
        }
    }, [_items, currentIndex])

    return <nav ref={el => { setMenu(el) }} id="menu_accesibility" className="menu-access" aria-label="Menú accesible">
        <h3 className="mb-4">Menu de accesibilidad</h3>
        <ul role="menu" style={{ listStyle: "none", padding: '0', margin: '0' }}>
            {items.length === 0 && <li>
                <Button variant="light" tabIndex={'-1'} role="menuitem" onClick={() => { setCurrentIndex(-1); document.getElementById("skip-link")?.focus(); }}>
                    Aceptar
                </Button>
            </li>}
            {items?.map((el, index) => {
                return <li>
                    <Button variant="light" tabIndex={'-1'} onFocus={index !== 0 ? undefined : () => { setCurrentIndex(0) }} role="menuitem" onClick={() => {
                        if (el.onClick != null) {
                            el.onClick()
                        } else if (el.anchor != null) {
                            const anchor = document.getElementById(el.anchor);
                            if (anchor) {
                                anchor.scrollIntoView({ behavior: "smooth", block: "start" });
                                anchor.setAttribute("tabindex", "-1");
                                anchor.focus();
                            }
                        }
                    }}>
                        {el.label}
                    </Button>
                </li>
            })}
        </ul>
        {items.length === 0 && <p>
            Este menu se encuentra vacío. Navega con la tecla "Tab".
        </p>}

        <p className="mt-4">Interactúa con los elementos del menú con las flechas del teclado. Presione "Esc" para salir</p>
    </nav>
}