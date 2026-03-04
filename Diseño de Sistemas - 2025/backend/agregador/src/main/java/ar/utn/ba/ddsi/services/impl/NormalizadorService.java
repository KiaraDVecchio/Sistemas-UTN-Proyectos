package ar.utn.ba.ddsi.services.impl;

import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.Ubicacion; // Asegúrate de importar esto
import ar.utn.ba.ddsi.normalizadores.NormalizadorCategoria;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NormalizadorService {

    private final NormalizadorCategoria normalizadorCategoria;
    private final GeorefService georefService;

    private static final List<String> CONECTORES = List.of("de", "del", "la", "las", "el", "los", "y", "en", "o");

    public NormalizadorService(NormalizadorCategoria normalizadorCategoria, GeorefService georefService){
        this.normalizadorCategoria = normalizadorCategoria;
        this.georefService = georefService;
    }

    public void normalizar(Hecho hecho) {
        if (hecho == null) return;

        if (hecho.getCategoria() != null) {
            var catNormalizada = normalizadorCategoria.normalizarCategoria(hecho.getCategoria());

//            if (catNormalizada.getNombre() != null) {
//                String nombreLindo = formatearTexto(catNormalizada.getNombre());
//                catNormalizada.setNombre(nombreLindo);
//            }
            hecho.setCategoria(catNormalizada);
        }


//        if (hecho.getUbicacion() == null) {
//            hecho.setUbicacion(new Ubicacion());
//        }
//
//        Ubicacion ubicacion = hecho.getUbicacion();
//        Double lat = ubicacion.getLatitud();
//        Double lon = ubicacion.getLongitud();
//        String provActual = ubicacion.getProvincia();
//
//        if (provActual != null && !provActual.isBlank()) {
//            String provFormateada = formatearTexto(provActual);
//            ubicacion.setProvincia(provFormateada);
//        }
//        else {
//            String nuevaProvincia = "Ubicación externa";
//
//            if (lat != null && lon != null) {
//                String provDetectada = georefService.obtenerProvincia(lat, lon);
//                if (provDetectada != null) {
//                    nuevaProvincia = provDetectada;
//                }
//            }
//
//            ubicacion.setProvincia(nuevaProvincia);
//        }
    }

    private String formatearTexto(String texto) {
        if (texto == null || texto.isBlank()) return texto;

        String[] palabras = texto.trim().toLowerCase().split("\\s+");
        StringBuilder resultado = new StringBuilder();

        for (int i = 0; i < palabras.length; i++) {
            String palabra = palabras[i];

            if (i == 0 || !CONECTORES.contains(palabra)) {
                if (palabra.length() > 1) {
                    resultado.append(Character.toUpperCase(palabra.charAt(0)))
                            .append(palabra.substring(1));
                } else {
                    resultado.append(Character.toUpperCase(palabra.charAt(0)));
                }
            } else {
                resultado.append(palabra);
            }

            if (i < palabras.length - 1) {
                resultado.append(" ");
            }
        }
        return resultado.toString();
    }
}