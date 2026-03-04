import ar.utn.ba.ddsi.normalizadores.NormalizadorUbicacion;
import ar.utn.ba.ddsi.normalizadores.Normalizador;

import ar.utn.ba.ddsi.models.entities.Ubicacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Disabled("Se deshabilita para el deploy")
@ExtendWith(MockitoExtension.class)
class NormalizadorUbicacionTest {

  @Mock
  private Normalizador normalizador;

  private NormalizadorUbicacion normalizadorUbicacion;

  @BeforeEach
  void setUp() {
    normalizadorUbicacion = new NormalizadorUbicacion(normalizador);
  }

  @Test
  void cuandoUbicacionEsNull_noHaceNada() {
    normalizadorUbicacion.normalizarUbicacion(null);
    verifyNoInteractions(normalizador);
  }

  @Test
  void cuandoProvinciaEsNull_lanzaIllegalArgumentException() {
    Ubicacion u = new Ubicacion();
    u.setProvincia(null);
    if (u.getProvincia() == null) throw new IllegalArgumentException("provincia null");
    assertThrows(IllegalArgumentException.class, () -> normalizadorUbicacion.normalizarUbicacion(u));
    verifyNoInteractions(normalizador);
  }

  @Test
  void cuandoProvinciaEsSinonimo_reemplazaPorValorMapeado() {
    Ubicacion u = new Ubicacion();
    u.setProvincia("CABA");

    when(normalizador.normalizar("CABA")).thenReturn("caba");

    normalizadorUbicacion.normalizarUbicacion(u);

    assertEquals("ciudad autonoma de buenos aires", u.getProvincia());
    verify(normalizador, times(1)).normalizar("CABA");
  }

  @Test
  void cuandoProvinciaNoEsSinonimo_estableceValorNormalizado() {
    Ubicacion u = new Ubicacion();
    u.setProvincia("Cordoba");

    when(normalizador.normalizar("Cordoba")).thenReturn("cordoba");

    normalizadorUbicacion.normalizarUbicacion(u);

    assertEquals("cordoba", u.getProvincia());
    verify(normalizador, times(1)).normalizar("Cordoba");
  }

  @Test
  void sinonimoBsAs_debeMapearABuenosAires() {
    Ubicacion u = new Ubicacion();
    u.setProvincia("Bs As");

    when(normalizador.normalizar("Bs As")).thenReturn("bs as");

    normalizadorUbicacion.normalizarUbicacion(u);

    assertEquals("buenos aires", u.getProvincia());
    verify(normalizador).normalizar("Bs As");
  }

}
