/*import ar.utn.ba.ddsi.algoritmos.impl.ConsensoMayoriaSimple;
import ar.utn.ba.ddsi.models.entities.*;
import ar.utn.ba.ddsi.models.entities.enumerados.Origen;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoFuente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.*;

public class algoritmoMayoriaSimpleTests {

  private ConsensoMayoriaSimple algoritmo;
  private Fuente fuente1, fuente2, fuente3, fuente4;

  @BeforeEach
  void setUp() {
    algoritmo = new ConsensoMayoriaSimple();

    fuente1 = new Fuente("https://diario.com", TipoFuente.DINAMICA);
    fuente2 = new Fuente("https://revista.com", TipoFuente.ESTATICA);
    fuente3 = new Fuente("https://proxy.com", TipoFuente.PROXY);
    fuente4 = new Fuente("https://otra.com", TipoFuente.DINAMICA);
  }

  @Test
  void test_mayoria_simple_con_mitad_igual_consensua() {
    // Arrange: 4 fuentes, 2 reportan el mismo hecho (la mitad)
    Set<Fuente> fuentes = Set.of(fuente1, fuente2, fuente3, fuente4);
    Coleccion coleccion = new Coleccion("Test Mayoría Simple", "Descripción", fuentes);

    Hecho hecho1 = new Hecho("Evento A", "Detalle común",
        new Categoria("General"),
        new Ubicacion(-34.0, -58.0),
        LocalDate.of(2025, 1, 1),
        Origen.CARGA_MANUAL,
        "reporteroA");

    Hecho hecho2 = new Hecho("Evento A", "Detalle común",
        new Categoria("General"),
        new Ubicacion(-34.0, -58.0),
        LocalDate.of(2025, 1, 1),
        Origen.CARGA_MANUAL,
        "reporteroB");

    Hecho hecho3 = new Hecho("Evento B", "Detalle distinto",
        new Categoria("General"),
        new Ubicacion(-34.0, -58.0),
        LocalDate.of(2025, 1, 2),
        Origen.CARGA_MANUAL,
        "reporteroC");

    Hecho hecho4 = new Hecho("Evento C", "Otro detalle",
        new Categoria("General"),
        new Ubicacion(-34.0, -58.0),
        LocalDate.of(2025, 1, 3),
        Origen.CARGA_MANUAL,
        "reporteroD");

    coleccion.agregarHechos(List.of(hecho1, hecho2, hecho3, hecho4));

    Map<Fuente, List<Hecho>> hechosPorFuente = Map.of(
        fuente1, List.of(hecho1),
        fuente2, List.of(hecho2),
        fuente3, List.of(hecho3),
        fuente4, List.of(hecho4)
    );

    // Act
    algoritmo.calcularConsenso(coleccion, hechosPorFuente);

    // Assert:
    // hecho1 y hecho2 son iguales y representan la mitad (2/4), deberían consensuarse
    assertTrue(hecho1.isConsensuado());
    assertTrue(hecho2.isConsensuado());

    // hecho3 y hecho4 no tienen mayoría, no deberían consensuarse
    assertFalse(hecho3.isConsensuado());
    assertFalse(hecho4.isConsensuado());
  }

  @Test
  void test_mayoria_simple_con_menos_de_mitad_no_consensua() {
    // Arrange: 3 fuentes, solo 1 reporta un hecho
    Set<Fuente> fuentes = Set.of(fuente1, fuente2, fuente3);
    Coleccion coleccion = new Coleccion("Test Mayoría Simple", "Descripción", fuentes);

    Hecho hecho1 = new Hecho("Evento Único", "Solo un reporte",
        new Categoria("General"),
        new Ubicacion(-34.0, -58.0),
        LocalDate.of(2025, 2, 2),
        Origen.CARGA_MANUAL,
        "reporteroA");

    coleccion.agregarHechos(List.of(hecho1));

    Map<Fuente, List<Hecho>> hechosPorFuente = Map.of(
        fuente1, List.of(hecho1),
        fuente2, List.of(),
        fuente3, List.of()
    );

    // Act
    algoritmo.calcularConsenso(coleccion, hechosPorFuente);

    // Assert: no hay mayoría (1 < 3/2), no consensuar
    assertFalse(hecho1.isConsensuado());
  }

  @Test
  void test_mayoria_simple_con_todas_las_fuentes_consensua() {
    // Arrange: 3 fuentes reportan el mismo hecho
    Set<Fuente> fuentes = Set.of(fuente1, fuente2, fuente3);
    Coleccion coleccion = new Coleccion("Test Mayoría Simple", "Descripción", fuentes);

    Hecho hecho1 = new Hecho("Evento Total", "Todos reportan igual",
        new Categoria("General"),
        new Ubicacion(-34.0, -58.0),
        LocalDate.of(2025, 3, 3),
        Origen.CARGA_MANUAL,
        "reporteroA");

    Hecho hecho2 = new Hecho("Evento Total", "Todos reportan igual",
        new Categoria("General"),
        new Ubicacion(-34.0, -58.0),
        LocalDate.of(2025, 3, 3),
        Origen.CARGA_MANUAL,
        "reporteroB");

    Hecho hecho3 = new Hecho("Evento Total", "Todos reportan igual",
        new Categoria("General"),
        new Ubicacion(-34.0, -58.0),
        LocalDate.of(2025, 3, 3),
        Origen.CARGA_MANUAL,
        "reporteroC");

    coleccion.agregarHechos(List.of(hecho1, hecho2, hecho3));

    Map<Fuente, List<Hecho>> hechosPorFuente = Map.of(
        fuente1, List.of(hecho1),
        fuente2, List.of(hecho2),
        fuente3, List.of(hecho3)
    );

    // Act
    algoritmo.calcularConsenso(coleccion, hechosPorFuente);

    // Assert: todos consensuados
    assertTrue(hecho1.isConsensuado());
    assertTrue(hecho2.isConsensuado());
    assertTrue(hecho3.isConsensuado());
  }

  @Test
  void test_mayoria_simple_sin_hechos_no_consensua() {
    // Arrange: colección vacía con fuentes
    Set<Fuente> fuentes = Set.of(fuente1, fuente2);
    Coleccion coleccion = new Coleccion("Test Mayoría Simple", "Descripción", fuentes);

    coleccion.agregarHechos(Collections.emptyList());

    Map<Fuente, List<Hecho>> hechosPorFuente = Map.of(
        fuente1, List.of(),
        fuente2, List.of()
    );

    // Act
    algoritmo.calcularConsenso(coleccion, hechosPorFuente);

    // Assert: nada para consensuar, ningún error
    assertTrue(coleccion.getHechos().isEmpty());
  }
}*/

