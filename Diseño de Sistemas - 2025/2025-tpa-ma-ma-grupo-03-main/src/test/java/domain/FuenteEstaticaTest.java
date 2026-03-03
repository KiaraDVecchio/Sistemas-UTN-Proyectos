package domain;

import static org.junit.jupiter.api.Assertions.*;

import domain.criterios.CriterioCategoria;
import domain.criterios.CriterioFechaAcontecimiento;
import domain.criterios.CriterioTitulo;
import domain.fuentes.FuenteEstatica;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

class FuenteEstaticaTest {
  private FuenteEstatica fuente;
  private Hecho h1;

  @Test
  @DisplayName("Tests de fuenteEstatica")
  public void cargarHechos() {
    this.fuente = new FuenteEstatica("src/test/java/domain/desastres_naturales_argentina.csv");

    fuente.leerHechos();

    assertEquals("Ráfagas de más de 100 km/h causa estragos en San Vicente, Misiones", fuente.getHechosCargados().get(0).getTitulo());
    assertEquals(10,fuente.getHechosCargados().size());
    System.out.println(fuente.getHechosCargados().get(0).getTitulo());
    System.out.println(fuente.getHechosCargados().get(1).getFechaAcontecimiento());
    System.out.println(fuente.getHechosCargados().get(2).getFechaAcontecimiento());
    System.out.println(fuente.getHechosCargados().size());
  }
}