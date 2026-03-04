import { Skeleton } from '../../../components/shared/Skeleton'
import './style.css'

export const SkeletonAlojamientos = () => {
    return <div className="alojamiento-card__skeleton--list">
        <div className="alojamiento-card__skeleton--container"><Skeleton /></div>
        <div className="alojamiento-card__skeleton--container"><Skeleton /></div>
        <div className="alojamiento-card__skeleton--container"><Skeleton /></div>
        <div className="alojamiento-card__skeleton--container"><Skeleton /></div>
        <div className="alojamiento-card__skeleton--container"><Skeleton /></div>
        <div className="alojamiento-card__skeleton--container"><Skeleton /></div>
        <div className="alojamiento-card__skeleton--container"><Skeleton /></div>
        <div className="alojamiento-card__skeleton--container"><Skeleton /></div>
        <div className="alojamiento-card__skeleton--container"><Skeleton /></div>
        <div className="alojamiento-card__skeleton--container"><Skeleton /></div>
    </div>
}