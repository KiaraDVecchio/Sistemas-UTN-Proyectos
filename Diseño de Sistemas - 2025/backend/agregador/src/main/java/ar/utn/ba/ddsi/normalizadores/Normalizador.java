package ar.utn.ba.ddsi.normalizadores;

import org.springframework.stereotype.Component;
import java.text.Normalizer;

@Component
public class Normalizador {

  public String normalizar(String s) {
    if(s == null) {
      return null;
    }
    String normalizado = Normalizer.normalize(s.trim().toLowerCase(), Normalizer.Form.NFD) //NFD separa la base de la palabra de sus tildes
        .replaceAll("\\p{M}+","");  //regex que saca las tildes y las remplaza por ""
    return normalizado.replaceAll("\\s+"," "); //junta varios espacios del medio si es que hay y los remplaza por un solo espacio
    }

}
