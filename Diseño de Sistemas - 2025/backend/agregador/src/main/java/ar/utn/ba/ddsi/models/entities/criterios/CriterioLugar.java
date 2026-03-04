package ar.utn.ba.ddsi.models.entities.criterios;

import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.Ubicacion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("lugar")
@Getter @Setter
public class CriterioLugar extends Criterio {

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitud", column = @Column(name = "criterio_latitud")),
            @AttributeOverride(name = "longitud", column = @Column(name = "criterio_longitud")),
            @AttributeOverride(name = "provincia", column = @Column(name = "criterio_provincia"))
    })
    private Ubicacion ubicacion;

    @Column(name = "rango_maximo", nullable = true)
    private int rangoMaximo;

    @Column(name = "provincia_buscada", nullable = true)
    private String provinciaBuscada;

    public CriterioLugar() {}

    public CriterioLugar(Ubicacion ubicacion, int rangoMaximo, String provinciaBuscada) {
        this.ubicacion = ubicacion;
        this.rangoMaximo = rangoMaximo;
        this.provinciaBuscada = provinciaBuscada;
    }

    @Override
    public boolean cumpleCriterio(Hecho hecho) {
        if (hecho.getUbicacion() == null) return false;

        boolean busquedaPorMapaActiva = this.ubicacion != null
                && this.ubicacion.getLatitud() != null
                && this.ubicacion.getLongitud() != null
                && this.rangoMaximo >= 0;

        if (busquedaPorMapaActiva) {

            if (hecho.getUbicacion().getLatitud() == null || hecho.getUbicacion().getLongitud() == null) {
                return false;
            }

            double distanciaKm = calcularDistanciaHaversine(
                    this.ubicacion.getLatitud(), this.ubicacion.getLongitud(),
                    hecho.getUbicacion().getLatitud(), hecho.getUbicacion().getLongitud()
            );


            return distanciaKm <= this.rangoMaximo;
        }

        if (this.provinciaBuscada != null && !this.provinciaBuscada.isBlank()) {
            String provHecho = hecho.getUbicacion().getProvincia();
            if (provHecho == null) return false;
            return this.provinciaBuscada.equalsIgnoreCase(provHecho);
        }

        return true;
    }

    private double calcularDistanciaHaversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio Tierra KM
        double latDist = Math.toRadians(lat2 - lat1);
        double lonDist = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDist / 2) * Math.sin(latDist / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDist / 2) * Math.sin(lonDist / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}