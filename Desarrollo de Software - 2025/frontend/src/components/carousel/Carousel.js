import AlojamientoCard from '../alojamientos/AlojamientoCard';
import './Carousel.css'

function Carousel({ alojamientos }) {
    return (
        <div className="alojamientos-carousel">
            {alojamientos.map((alojamiento, index) =>
                <AlojamientoCard key={index} aAlojamiento={alojamiento} />
            )}
        </div>
    )
}

export default Carousel;