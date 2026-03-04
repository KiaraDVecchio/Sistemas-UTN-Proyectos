

import ar.utn.ba.ddsi.models.entities.Categoria;
import ar.utn.ba.ddsi.models.repositories.ICategoriaRepository;
import ar.utn.ba.ddsi.normalizadores.Normalizador;
import ar.utn.ba.ddsi.normalizadores.NormalizadorCategoria;
import ar.utn.ba.ddsi.services.IRaeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Disabled("Se deshabilita para el deploy")
@ExtendWith(MockitoExtension.class)
class NormalizadorCategoriaTest {

  @Mock ICategoriaRepository categoriaRepository;
  @Mock
  Normalizador normalizador;
  @Mock IRaeService diccionario;

  NormalizadorCategoria sut; // System Under Test

  @BeforeEach
  void setUp() {
    sut = new NormalizadorCategoria(categoriaRepository, normalizador, diccionario);
  }

  @Test
  void cuandoCategoriaEsNull_retornaNull_yNoInteractua() {
    assertNull(sut.normalizarCategoria(null));
    verifyNoInteractions(normalizador, categoriaRepository, diccionario);
  }

  @Test
  void cuandoNombreNormalizadoEsNullOLleno_lanzaIllegalArgumentException() {
    Categoria c = new Categoria(null);
    when(normalizador.normalizar(null)).thenReturn(null);

    IllegalArgumentException ex = assertThrows(
        IllegalArgumentException.class,
        () -> sut.normalizarCategoria(c)
    );
    assertTrue(ex.getMessage().toLowerCase().contains("invalido"));
    verify(normalizador).normalizar(null);
    verifyNoInteractions(categoriaRepository, diccionario);
  }

  @Test
  void cuandoCoincidePorAliasLocal_devuelveCategoriaDeRepositorio_yNoConsultaRAE() {
    // entrada "Fuego" -> normaliza "fuego" -> alias "incendio"
    Categoria entrada = new Categoria("Fuego");
    Categoria incendio = new Categoria("incendio");

    when(normalizador.normalizar("Fuego")).thenReturn("fuego");
    when(categoriaRepository.findByNombreIgnoreCase("incendio"))
        .thenReturn(Optional.of(incendio));

    Categoria out = sut.normalizarCategoria(entrada);

    assertSame(incendio, out);
    verify(normalizador).normalizar("Fuego");
    verify(categoriaRepository).findByNombreIgnoreCase("incendio");
    verifyNoInteractions(diccionario);
    verify(categoriaRepository, never()).save(any());
  }

  @Test
  void cuandoNoExistePeroSinonimoRAEExiste_devuelvePorSinonimo_yNoCrea() {
    // entrada "llamarada" -> no alias, no match directo -> RAE sinónimos = ["incendio"]
    Categoria entrada = new Categoria("llamarada");
    Categoria incendio = new Categoria("incendio");

    when(normalizador.normalizar("llamarada")).thenReturn("llamarada");
    when(categoriaRepository.findByNombreIgnoreCase("llamarada"))
        .thenReturn(Optional.empty());
    when(diccionario.sinonimos("llamarada"))
        .thenReturn(Set.of("incendio")); // ya normalizados
    when(categoriaRepository.findByNombreIgnoreCase("incendio"))
        .thenReturn(Optional.of(incendio));

    Categoria out = sut.normalizarCategoria(entrada);

    assertSame(incendio, out);
    verify(categoriaRepository, never()).save(any());
  }

  @Test
  void cuandoNoExisteNiSinonimos_creaCategoriaNueva() {
    Categoria entrada = new Categoria("Nueva  Cat");
    when(normalizador.normalizar("Nueva  Cat")).thenReturn("nueva cat");
    when(categoriaRepository.findByNombreIgnoreCase("nueva cat"))
        .thenReturn(Optional.empty());              // no existe
    when(diccionario.sinonimos("nueva cat"))
        .thenReturn(Set.of());                      // sin sinónimos
    // crea
    ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
    when(categoriaRepository.save(any(Categoria.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    Categoria out = sut.normalizarCategoria(entrada);

    verify(categoriaRepository).save(captor.capture());
    assertEquals("nueva cat", captor.getValue().getNombre());
    assertEquals("nueva cat", out.getNombre());
  }

  @Test
  void cuandoSaveFallaPorDuplicado_reintentaLeyendoYDevuelveLaExistente() {
    Categoria entrada = new Categoria("Incendio Forestal");
    when(normalizador.normalizar("Incendio Forestal")).thenReturn("incendio forestal");

    // no existe al principio
    when(categoriaRepository.findByNombreIgnoreCase("incendio forestal"))
        .thenReturn(Optional.empty())               // pre-save
        .thenReturn(Optional.of(new Categoria("incendio forestal"))); // post-save (otro hilo la creó)

    when(diccionario.sinonimos("incendio forestal"))
        .thenReturn(Set.of());                      // no aporta

    // Simular carrera: save lanza violación de único
    when(categoriaRepository.save(any(Categoria.class)))
        .thenThrow(new DataIntegrityViolationException("dup"));

    Categoria out = sut.normalizarCategoria(entrada);

    assertEquals("incendio forestal", out.getNombre());
    verify(categoriaRepository, times(2)).findByNombreIgnoreCase("incendio forestal");
    verify(categoriaRepository).save(any(Categoria.class));
  }
  // --- Fake muy simple que “simula” la RAE y muestra por consola ---
  static class ConsoleIRaeService implements ar.utn.ba.ddsi.services.IRaeService {
    @Override
    public java.util.Optional<String> lemaConAcento(String termino) {
      System.out.println("[RAE] lemaConAcento(" + termino + ")");
      return java.util.Optional.empty(); // no usamos el lema en tu flujo actual
    }

    @Override
    public java.util.Set<String> sinonimos(String termino) {
      System.out.println("[RAE] sinonimos(" + termino + ")");
      // Devolvemos un sinónimo que sí existe en tu base (p.ej. "incendio")
      return java.util.Set.of("incendio"); // ya “normalizado”
    }
  }

  // --- Test “demo” que usa la fake RAE y muestra por consola ---
  @Test
  void demoLlamadaRae_porConsola_yMatchPorSinonimo() {
    // Arrange
    var entrada = new ar.utn.ba.ddsi.models.entities.Categoria("llamarada");

    // normaliza el texto de entrada a minúsculas/sin tildes
    when(normalizador.normalizar("llamarada")).thenReturn("llamarada");

    // no hay match directo en DB ni por alias local
    when(categoriaRepository.findByNombreIgnoreCase("llamarada"))
        .thenReturn(java.util.Optional.empty());

    // inyectamos la “RAE” de consola que devuelve el sinónimo "incendio"
    this.sut = new NormalizadorCategoria(categoriaRepository, normalizador, new ConsoleIRaeService());

    // el sinónimo SÍ existe en la base
    when(categoriaRepository.findByNombreIgnoreCase("incendio"))
        .thenReturn(java.util.Optional.of(new ar.utn.ba.ddsi.models.entities.Categoria("incendio")));

    // Act
    var out = sut.normalizarCategoria(entrada);

    // Assert
    assertNotNull(out);
    assertEquals("incendio", out.getNombre());

    // Verifica que se normalizó la entrada y que NO se intentó crear categoría
    verify(normalizador).normalizar("llamarada");
    verify(categoriaRepository, never()).save(any());
  }


}

