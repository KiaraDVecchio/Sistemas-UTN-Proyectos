package domain.fuentes;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import domain.Categoria;
import domain.Hecho;
import domain.Ubicacion;
import domain.enumerados.Origen;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import lombok.Getter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class FuenteEstatica extends Fuente {
  @Getter private String ruta;
  @Getter private List<Hecho> hechosCargados; //historial , pusimos lista para el test

  public FuenteEstatica(String ruta) {
    this.ruta = ruta;
    this.hechosCargados = new ArrayList<>();
  }

  public void CambiarRuta(String string){
    this.ruta = string;
  }

  public void cargarHechos(Hecho ... nuevosHechos) {
    Collections.addAll(hechosCargados, nuevosHechos);
    System.out.println("Los hechos fueron cargados");
  }


  @Override
  public List<Hecho> getHechos() {
    return hechosCargados;
  }

  @Override
  public void leerHechos() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");

    try (CSVReader reader = new CSVReader(new FileReader(this.ruta))) {
      String[] fila;
      reader.readNext(); //lee la primer fila y la saltea

      while ((fila = reader.readNext()) != null) {
        System.out.println("La direccion esta bien");
        String titulo = fila[0];
        String descripcion = fila[1];
        Categoria categoria = new Categoria(fila[2]);
        Ubicacion ubicacion = new Ubicacion(Double.parseDouble(fila[3]), Double.parseDouble(fila[4]));
        LocalDate fechaAcontecimiento = LocalDate.parse(fila[5], formatter);

        Hecho hechoACargar = new Hecho(
            titulo, descripcion, categoria, ubicacion, fechaAcontecimiento,
            Origen.PROVENIENTE_DE_DATASET
        );

        //Un hecho está repetido si el “título” es el mismo. Se pisarán los atributos del existente
        this.hechosCargados.removeIf(hecho -> hecho.getTitulo().equals(hechoACargar.getTitulo()));
        this.hechosCargados.add(hechoACargar); //lo agrega al historial
      }

    } catch (IOException | CsvValidationException e) {
      throw new RuntimeException("No se pudo leer el archivo CSV");
    }
  }
}
