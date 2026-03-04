/*import ar.utn.ba.ddsi.algoritmos.impl.ConsensoMultiplesMenciones;
import ar.utn.ba.ddsi.models.entities.*;
import ar.utn.ba.ddsi.models.entities.enumerados.Origen;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoFuente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.*;

public class algoritmoMultiplesMencionesTests {

    private ConsensoMultiplesMenciones algoritmo;
    private Fuente fuente1, fuente2, fuente3;

    @BeforeEach
    void setUp() {
      algoritmo = new ConsensoMultiplesMenciones();

      fuente1 = new Fuente("https://diario.com", TipoFuente.DINAMICA);
      fuente2 = new Fuente("https://revista.com", TipoFuente.ESTATICA);
      fuente3 = new Fuente("https://proxy.com", TipoFuente.PROXY);
    }

    @Test
    void test_dos_fuentes_mismo_hecho_debe_consensuar() {
      // Arrange: 2 fuentes reportan exactamente el mismo hecho
      Set<Fuente> fuentes = Set.of(fuente1, fuente2);
      Coleccion coleccion = new Coleccion("Test", "Descripción", fuentes);

      Hecho hecho1 = new Hecho("Lluvia en CABA", "Llovió mucho",
          new Categoria("Clima"),
          new Ubicacion(-34.6037, -58.3816),
          LocalDate.of(2024, 1, 15),
          Origen.CARGA_MANUAL,
          "testigo1");

      Hecho hecho2 = new Hecho("Lluvia en CABA", "Llovió mucho",
          new Categoria("Clima"),
          new Ubicacion(-34.6037, -58.3816),
          LocalDate.of(2024, 1, 15),
          Origen.CARGA_MANUAL,
          "testigo1");

      Map<Fuente, List<Hecho>> hechosPorFuente = Map.of(
          fuente1, List.of(hecho1),
          fuente2, List.of(hecho2)
      );

      // Act
      algoritmo.calcularConsenso(coleccion, hechosPorFuente);

      // Assert: ambos hechos deben estar consensuados
      assertTrue(hecho1.isConsensuado());
      assertTrue(hecho2.isConsensuado());
    }

    @Test
    void test_una_sola_fuente_no_debe_consensuar() {
      // Arrange: solo 1 fuente reporta el hecho
      Set<Fuente> fuentes = Set.of(fuente1, fuente2);
      Coleccion coleccion = new Coleccion("Test", "Descripción", fuentes);

      Hecho hecho1 = new Hecho("Accidente en Córdoba", "Choque múltiple",
          new Categoria("Accidentes"),
          new Ubicacion(-31.4201, -64.1888),
          LocalDate.of(2024, 2, 10),
          Origen.PROVENIENTE_DE_DATASET,
          "policía");

      Map<Fuente, List<Hecho>> hechosPorFuente = Map.of(
          fuente1, List.of(hecho1),
          fuente2, List.of() // fuente2 no tiene este hecho
      );

      // Act
      algoritmo.calcularConsenso(coleccion, hechosPorFuente);

      // Assert: no debe estar consensuado
      assertFalse(hecho1.isConsensuado());
    }

    @Test
    void test_mismo_titulo_pero_diferente_contenido_no_debe_consensuar() {
      // Arrange: 2 fuentes reportan hechos con mismo título pero diferentes detalles
      Set<Fuente> fuentes = Set.of(fuente1, fuente2);
      Coleccion coleccion = new Coleccion("Test", "Descripción", fuentes);

      Hecho hecho1 = new Hecho("Protesta en Plaza", "Protesta pacífica",
          new Categoria("Política"),
          new Ubicacion(-34.6037, -58.3816),
          LocalDate.of(2024, 3, 8),
          Origen.CARGA_MANUAL,
          "reportero1");

      Hecho hecho2 = new Hecho("Protesta en Plaza", "Protesta violenta", // <- diferente descripción
          new Categoria("Política"),
          new Ubicacion(-34.6037, -58.3816),
          LocalDate.of(2024, 3, 8),
          Origen.CARGA_MANUAL,
          "reportero2");

      Map<Fuente, List<Hecho>> hechosPorFuente = Map.of(
          fuente1, List.of(hecho1),
          fuente2, List.of(hecho2)
      );

      // Act
      algoritmo.calcularConsenso(coleccion, hechosPorFuente);

      // Assert: no debe consensuar por tener contenido diferente
      assertFalse(hecho1.isConsensuado());
      assertFalse(hecho2.isConsensuado());
    }

    @Test
    void test_tres_fuentes_mismo_hecho_debe_consensuar() {
      // Arrange: 3 fuentes reportan el mismo hecho
      Set<Fuente> fuentes = Set.of(fuente1, fuente2, fuente3);
      Coleccion coleccion = new Coleccion("Test", "Descripción", fuentes);

      Hecho hecho1 = new Hecho("Inauguración museo", "Nuevo museo de arte",
          new Categoria("Cultura"),
          new Ubicacion(-32.9442, -60.6505),
          LocalDate.of(2024, 4, 20),
          Origen.PROVISTO_POR_CONTRIBUYENTE,
          "municipio");

      Hecho hecho2 = new Hecho("Inauguración museo", "Nuevo museo de arte",
          new Categoria("Cultura"),
          new Ubicacion(-32.9442, -60.6505),
          LocalDate.of(2024, 4, 20),
          Origen.PROVISTO_POR_CONTRIBUYENTE,
          "municipio");

      Hecho hecho3 = new Hecho("Inauguración museo", "Nuevo museo de arte",
          new Categoria("Cultura"),
          new Ubicacion(-32.9442, -60.6505),
          LocalDate.of(2024, 4, 20),
          Origen.PROVISTO_POR_CONTRIBUYENTE,
          "municipio");

      Map<Fuente, List<Hecho>> hechosPorFuente = Map.of(
          fuente1, List.of(hecho1),
          fuente2, List.of(hecho2),
          fuente3, List.of(hecho3)
      );

      // Act
      algoritmo.calcularConsenso(coleccion, hechosPorFuente);

      // Assert: todos deben estar consensuados
      assertTrue(hecho1.isConsensuado());
      assertTrue(hecho2.isConsensuado());
      assertTrue(hecho3.isConsensuado());
    }

    @Test
    void test_multiples_hechos_algunos_consensuados_otros_no() {
      // Arrange: varios hechos, algunos con consenso y otros sin él
      Set<Fuente> fuentes = Set.of(fuente1, fuente2, fuente3);
      Coleccion coleccion = new Coleccion("Test", "Descripción", fuentes);

      // Hecho A: consensuado (2 fuentes iguales)
      Hecho hechoA1 = new Hecho("Festival de música", "Rock en el parque",
          new Categoria("Música"),
          new Ubicacion(-34.6037, -58.3816),
          LocalDate.of(2024, 5, 1),
          Origen.CARGA_MANUAL,
          "organizador");

      Hecho hechoA2 = new Hecho("Festival de música", "Rock en el parque",
          new Categoria("Música"),
          new Ubicacion(-34.6037, -58.3816),
          LocalDate.of(2024, 5, 1),
          Origen.CARGA_MANUAL,
          "organizador");

      // Hecho B: no consensuado (solo 1 fuente)
      Hecho hechoB1 = new Hecho("Venta de casa", "Casa antigua en venta",
          new Categoria("Inmuebles"),
          new Ubicacion(-31.4201, -64.1888),
          LocalDate.of(2024, 5, 2),
          Origen.PROVENIENTE_DE_DATASET,
          "inmobiliaria");

      Map<Fuente, List<Hecho>> hechosPorFuente = Map.of(
          fuente1, List.of(hechoA1, hechoB1),
          fuente2, List.of(hechoA2),
          fuente3, List.of()
      );

      // Act
      algoritmo.calcularConsenso(coleccion, hechosPorFuente);

      // Assert
      assertTrue(hechoA1.isConsensuado());  // Festival consensuado
      assertTrue(hechoA2.isConsensuado());
      assertFalse(hechoB1.isConsensuado()); // Venta no consensuada
    }

    @Test
    void test_coleccion_con_menos_de_dos_fuentes_no_consensua_nada() {
      // Arrange: colección con solo 1 fuente
      Set<Fuente> fuentes = Set.of(fuente1);
      Coleccion coleccion = new Coleccion("Test", "Descripción", fuentes);

      Hecho hecho1 = new Hecho("Evento único", "Descripción única",
          new Categoria("Test"),
          new Ubicacion(-34.6037, -58.3816),
          LocalDate.of(2024, 6, 1),
          Origen.CARGA_MANUAL,
          "fuente única");

      coleccion.agregarHechos(List.of(hecho1));


      Map<Fuente, List<Hecho>> hechosPorFuente = Map.of(
          fuente1, List.of(hecho1)
      );

      // Act
      algoritmo.calcularConsenso(coleccion, hechosPorFuente);

      // Assert: no debe consensuar con menos de 2 fuentes
      assertFalse(hecho1.isConsensuado());
    }
  }
*/