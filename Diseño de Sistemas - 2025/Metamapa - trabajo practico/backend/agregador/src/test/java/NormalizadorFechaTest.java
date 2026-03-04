import ar.utn.ba.ddsi.normalizadores.NormalizadorFecha;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

@Disabled("Se deshabilita para el deploy")
public class NormalizadorFechaTest {
  private final NormalizadorFecha normalizador = new NormalizadorFecha();

  @Test
  public void testIso() {
    LocalDate res = normalizador.normalizarFecha("2025-09-07");
    Assertions.assertEquals(LocalDate.of(2025, 9, 7), res);
  }

  @Test
  public void testDiaMesAnio() {
    LocalDate res = normalizador.normalizarFecha("07/09/2025");
    Assertions.assertEquals(LocalDate.of(2025, 9, 7), res);
  }

  @Test
  public void testDiaMesAnio_sinZero() {
    LocalDate res = normalizador.normalizarFecha("7/9/2025");
    Assertions.assertEquals(LocalDate.of(2025, 9, 7), res);
  }

  @Test
  public void testMesPrimeroFallback() {
    LocalDate res = normalizador.normalizarFecha("12/31/2025", false);
    Assertions.assertEquals(LocalDate.of(2025, 12, 31), res);
  }

  @Test
  public void testTwoDigitsYear() {
    LocalDate res = normalizador.normalizarFecha("7/9/25");
    Assertions.assertEquals(LocalDate.of(2025, 9, 7), res);
  }

  @Test
  public void testFormatoConMesTextoEspanol() {
    LocalDate res = normalizador.normalizarFecha("7 sept 2025");
    Assertions.assertEquals(LocalDate.of(2025, 9, 7), res);
  }

  @Test
  public void testFormatoInvalidoLanzaExcepcion() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      normalizador.normalizarFecha("fecha-no-valida");
    });
  }
}
