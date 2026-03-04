/*import ar.utn.ba.ddsi.algoritmos.impl.ConsensoAbsoluta;
import ar.utn.ba.ddsi.models.entities.*;
import ar.utn.ba.ddsi.models.entities.enumerados.Origen;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoFuente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.*;

public class algoritmoAbsolutaTests {

  private ConsensoAbsoluta algoritmo;
  private Fuente fuente1, fuente2, fuente3;

  @BeforeEach
  void setUp() {
    algoritmo = new ConsensoAbsoluta();

    fuente1 = new Fuente("https://diario.com", TipoFuente.DINAMICA);
    fuente2 = new Fuente("https://revista.com", TipoFuente.ESTATICA);
    fuente3 = new Fuente("https://proxy.com", TipoFuente.PROXY);
  }

  @Test
  void test_todas_las_fuentes_mismo_hecho_deben_consensuar() {
    Set<Fuente> fuentes = Set.of(fuente1, fuente2, fuente3);
    Coleccion coleccion = new Coleccion("Test", "Descripción", fuentes);

    Hecho hecho1 = new Hecho("Evento común", "Mismo detalle",
        new Categoria("General"),
        new Ubicacion(-34.0, -58.0),
        LocalDate.of(2024, 7, 1),
        Origen.CARGA_MANUAL,
        "reportero1");

    Hecho hecho2 = new Hecho("Evento común", "Mismo detalle",
        new Categoria("General"),
        new Ubicacion(-34.0, -58.0),
        LocalDate.of(2024, 7, 1),
        Origen.CARGA_MANUAL,
        "reportero2");

    Hecho hecho3 = new Hecho("Evento común", "Mismo detalle",
        new Categoria("General"),
        new Ubicacion(-34.0, -58.0),
        LocalDate.of(2024, 7, 1),
        Origen.CARGA_MANUAL,
        "reportero3");

    Map<Fuente, List<Hecho>> hechosPorFuente = Map.of(
        fuente1, List.of(hecho1),
        fuente2, List.of(hecho2),
        fuente3, List.of(hecho3)
    );

    algoritmo.calcularConsenso(coleccion, hechosPorFuente);

    assertTrue(hecho1.isConsensuado());
    assertTrue(hecho2.isConsensuado());
    assertTrue(hecho3.isConsensuado());
  }

  @Test
  void test_si_una_fuente_no_tiene_el_hecho_no_consensua() {
    Set<Fuente> fuentes = Set.of(fuente1, fuente2, fuente3);
    Coleccion coleccion = new Coleccion("Test", "Descripción", fuentes);

    Hecho hecho1 = new Hecho("Evento común", "Mismo detalle",
        new Categoria("General"),
        new Ubicacion(-34.0, -58.0),
        LocalDate.of(2024, 7, 1),
        Origen.CARGA_MANUAL,
        "reportero1");

    Hecho hecho2 = new Hecho("Evento común", "Mismo detalle",
        new Categoria("General"),
        new Ubicacion(-34.0, -58.0),
        LocalDate.of(2024, 7, 1),
        Origen.CARGA_MANUAL,
        "reportero2");

    Map<Fuente, List<Hecho>> hechosPorFuente = Map.of(
        fuente1, List.of(hecho1),
        fuente2, List.of(hecho2),
        fuente3, List.of()  // fuente3 no reporta el hecho
    );

    algoritmo.calcularConsenso(coleccion, hechosPorFuente);

    assertFalse(hecho1.isConsensuado());
    assertFalse(hecho2.isConsensuado());
  }

  @Test
  void test_si_una_fuente_tiene_hecho_diferente_no_consensua() {
    Set<Fuente> fuentes = Set.of(fuente1, fuente2, fuente3);
    Coleccion coleccion = new Coleccion("Test", "Descripción", fuentes);

    Hecho hecho1 = new Hecho("Evento común", "Detalle A",
        new Categoria("General"),
        new Ubicacion(-34.0, -58.0),
        LocalDate.of(2024, 7, 1),
        Origen.CARGA_MANUAL,
        "reportero1");

    Hecho hecho2 = new Hecho("Evento común", "Detalle A",
        new Categoria("General"),
        new Ubicacion(-34.0, -58.0),
        LocalDate.of(2024, 7, 1),
        Origen.CARGA_MANUAL,
        "reportero2");

    Hecho hecho3 = new Hecho("Evento común", "Detalle diferente",
        new Categoria("General"),
        new Ubicacion(-34.0, -58.0),
        LocalDate.of(2024, 7, 1),
        Origen.CARGA_MANUAL,
        "reportero3");

    Map<Fuente, List<Hecho>> hechosPorFuente = Map.of(
        fuente1, List.of(hecho1),
        fuente2, List.of(hecho2),
        fuente3, List.of(hecho3)
    );

    algoritmo.calcularConsenso(coleccion, hechosPorFuente);

    assertFalse(hecho1.isConsensuado());
    assertFalse(hecho2.isConsensuado());
    assertFalse(hecho3.isConsensuado());
  }

  @Test
  void test_unica_fuente_consensua_solo_si_es_unica_en_coleccion() {
    Set<Fuente> fuentes = Set.of(fuente1);
    Coleccion coleccion = new Coleccion("Test", "Descripción", fuentes);

    Hecho hecho1 = new Hecho("Evento único", "Detalle único",
        new Categoria("General"),
        new Ubicacion(-34.0, -58.0),
        LocalDate.of(2024, 7, 1),
        Origen.CARGA_MANUAL,
        "reportero1");

    Map<Fuente, List<Hecho>> hechosPorFuente = Map.of(
        fuente1, List.of(hecho1)
    );

    algoritmo.calcularConsenso(coleccion, hechosPorFuente);

    assertTrue(hecho1.isConsensuado());
  }

  @Test
  void test_multiples_hechos_varios_consensuados_y_no() {
    Set<Fuente> fuentes = Set.of(fuente1, fuente2, fuente3);
    Coleccion coleccion = new Coleccion("Test", "Descripción", fuentes);

    // Hecho consensuado por las 3 fuentes
    Hecho hechoA1 = new Hecho("Concierto", "Rock clásico",
        new Categoria("Música"),
        new Ubicacion(-34.6, -58.3),
        LocalDate.of(2024, 7, 2),
        Origen.CARGA_MANUAL,
        "fuenteA1");

    Hecho hechoA2 = new Hecho("Concierto", "Rock clásico",
        new Categoria("Música"),
        new Ubicacion(-34.6, -58.3),
        LocalDate.of(2024, 7, 2),
        Origen.CARGA_MANUAL,
        "fuenteA2");

    Hecho hechoA3 = new Hecho("Concierto", "Rock clásico",
        new Categoria("Música"),
        new Ubicacion(-34.6, -58.3),
        LocalDate.of(2024, 7, 2),
        Origen.CARGA_MANUAL,
        "fuenteA3");

    // Hecho no consensuado (fuente3 no lo reporta)
    Hecho hechoB1 = new Hecho("Exposición", "Arte moderno",
        new Categoria("Cultura"),
        new Ubicacion(-34.6, -58.3),
        LocalDate.of(2024, 7, 3),
        Origen.CARGA_MANUAL,
        "fuenteB1");

    Hecho hechoB2 = new Hecho("Exposición", "Arte moderno",
        new Categoria("Cultura"),
        new Ubicacion(-34.6, -58.3),
        LocalDate.of(2024, 7, 3),
        Origen.CARGA_MANUAL,
        "fuenteB2");

    Map<Fuente, List<Hecho>> hechosPorFuente = Map.of(
        fuente1, List.of(hechoA1, hechoB1),
        fuente2, List.of(hechoA2, hechoB2),
        fuente3, List.of(hechoA3) // fuente3 no reporta hechoB
    );

    algoritmo.calcularConsenso(coleccion, hechosPorFuente);

    // Hecho A debe estar consensuado
    assertTrue(hechoA1.isConsensuado());
    assertTrue(hechoA2.isConsensuado());
    assertTrue(hechoA3.isConsensuado());

    // Hecho B no consensuado (fuente3 no lo reporta)
    assertFalse(hechoB1.isConsensuado());
    assertFalse(hechoB2.isConsensuado());
  }
}
*/
