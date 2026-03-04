import './Footer.css'

function Footer(){
    return(
        <footer className='footer'>
            <div className='footer-section left'>
                <div className='contacto' >
                    <strong>Contactanos</strong>
                    <span>info@birbnb.com</span>
                </div>
            </div>

            <div className='footer-section center'>
                <strong>Seguinos</strong>
                <div className='redes'>
        
                <button className='facebook'>
                    <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="#1877F2"
                    >
                        <path d="M22 12c0-5.523-4.477-10-10-10S2 6.477 2 12c0 5.017 3.676 9.167 8.438 9.876v-6.987h-2.54v-2.89h2.54V9.797c0-2.507 1.492-3.89 3.777-3.89 1.094 0 2.238.195 2.238.195v2.465h-1.26c-1.243 0-1.63.771-1.63 1.562v1.875h2.773l-.443 2.89h-2.33v6.987C18.324 21.167 22 17.017 22 12z" />
                    </svg>
                </button>

                <button className='instagram'>
                    <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="#E4405F" viewBox="0 0 24 24">
                    <path d="M7.75 2h8.5A5.75 5.75 0 0 1 22 7.75v8.5A5.75 5.75 0 0 1 16.25 22h-8.5A5.75 5.75 0 0 1 2 16.25v-8.5A5.75 5.75 0 0 1 7.75 2Zm0 1.5A4.25 4.25 0 0 0 3.5 7.75v8.5A4.25 4.25 0 0 0 7.75 20.5h8.5a4.25 4.25 0 0 0 4.25-4.25v-8.5A4.25 4.25 0 0 0 16.25 3.5h-8.5Zm4.25 3.75a4.75 4.75 0 1 1 0 9.5a4.75 4.75 0 0 1 0-9.5Zm0 1.5a3.25 3.25 0 1 0 0 6.5a3.25 3.25 0 0 0 0-6.5Zm5.25-.5a1 1 0 1 1 0 2a1 1 0 0 1 0-2Z"/>
                    </svg>
                </button>

                <button className='X'>
                    <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="#000000" viewBox="0 0 24 24">
                    <path d="M13.33 10.2 20.41 2h-1.85l-6 7.02L7.23 2H2l7.5 10.73L2 22h1.85l6.37-7.45L16.77 22H22l-8.67-11.8zM4.27 3.5h2.2L19.73 20.5h-2.2L4.27 3.5z"/>
                    </svg>

                </button>
                </div>
            </div>

            <div className='footer-section right'>

                    <strong>Ayuda</strong>
                    <a href='#'>Centro de ayuda</a>
                    <a href='#'>Sobre nosotros</a>

            </div>

            <div className='footer-copy'>
                <p>© 2025 Birbnb, Inc</p>
            </div>
        </footer>
    )
}

export default Footer;