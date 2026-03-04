package ar.utn.ba.ddsi.normalizadores;

import ar.utn.ba.ddsi.models.entities.Ubicacion;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NormalizadorUbicacion {
  private final Normalizador normalizador;

  private static final Map<String,String> provinciaPorSinonimo = Map.ofEntries( //diccionario de sinonimos
      Map.entry("caba", "ciudad autonoma de buenos aires"),
      Map.entry("capital federal", "ciudad autonoma de buenos aires"),
      Map.entry("bs as", "buenos aires"),
      Map.entry("mdza", "mendoza")
  );

  public NormalizadorUbicacion(Normalizador normalizador){
    this.normalizador = normalizador;
  }

  public void normalizarUbicacion(Ubicacion ubicacion) {
    if(ubicacion == null) {
      return; //no hace nada
    }
    String provincia = ubicacion.getProvincia();
    if(provincia == null || provincia.isBlank()){
      //throw new IllegalArgumentException("Provincia invalida ");
      return;
    }

    String normalizada = normalizador.normalizar(ubicacion.getProvincia());

    ubicacion.setProvincia(provinciaPorSinonimo.getOrDefault(normalizada, normalizada));

  }

}

