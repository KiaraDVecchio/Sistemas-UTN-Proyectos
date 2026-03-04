package ar.utn.ba.ddsi.services.impl;

import ar.utn.ba.ddsi.services.IDetectorDeSpam;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DetectorDeSpam implements IDetectorDeSpam {

    private static final Pattern CARACTERES_REPETIDOS = Pattern.compile("(.)\\1{4,}");

    private static final List<String> PALABRAS_PROHIBIDAS = List.of(
            "spam_test",
            "casino online",
            "viagra",
            "ganar dinero",
            "trabaja desde casa",
            "bitcoin gratis",
            "criptomonedas",
            "préstamo inmediato",
            "sexo gratis",
            "oferta exclusiva",
            "haz clic aquí",
            "click here",
            "buy now",
            "free trial"
    );

    @Override
    public boolean esSpam(String texto) {
        if (texto == null) return true;

        if (texto.trim().length() < 500) {
            return true;
        }

        if (CARACTERES_REPETIDOS.matcher(texto).find()) {
            return true;
        }

        String[] palabras = texto.toLowerCase().split("\\s+");
        Set<String> palabrasUnicas = Arrays.stream(palabras).collect(Collectors.toSet());

        if (palabras.length > 0) {
            double ratioDiversidad = (double) palabrasUnicas.size() / palabras.length;
            if (palabras.length > 10 && ratioDiversidad < 0.10) {
                return true;
            }
        }

        String textoLower = texto.toLowerCase();

        return PALABRAS_PROHIBIDAS.stream()
                .anyMatch(textoLower::contains);
    }
}