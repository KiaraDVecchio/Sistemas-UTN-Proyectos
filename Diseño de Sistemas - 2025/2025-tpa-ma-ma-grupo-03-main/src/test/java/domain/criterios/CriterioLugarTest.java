package domain.criterios;

import domain.Categoria;
import domain.Hecho;
import domain.Ubicacion;
import domain.enumerados.Origen;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CriterioLugarTest {
  private Hecho h1;

  @Test
  @DisplayName("El hecho cumple con el criterio de Lugar")
  public void cumpleConCriterioLugar() {

    this.h1 = new Hecho(
        "Caída de aeronave impacta en Olavarría",
        "Grave caída de aeronave ocurrió en las inmediaciones de " +
            "Olavarría, Buenos Aires. El incidente provocó pánico entre " +
            "los residentes locales. Voluntarios de diversas organizaciones " +
            "se han sumado a las tareas de auxilio.",
        new Categoria("Caída de aeronave"),
        new Ubicacion(-36.86837, -60.343297),
        LocalDate.of(2001, 11, 29),
        Origen.CARGA_MANUAL);

    CriterioLugar unCriterio = new CriterioLugar( new Ubicacion(-22.86837,-59.343297), 30000);
    CriterioLugar otroCriterio = new CriterioLugar(new Ubicacion(-22.86837, -59.343297), 10);

    assertTrue(unCriterio.cumpleCriterio(h1));
    assertFalse(otroCriterio.cumpleCriterio(h1));
  }

}