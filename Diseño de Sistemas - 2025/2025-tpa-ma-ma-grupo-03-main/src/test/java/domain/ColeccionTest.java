package domain;

import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import domain.criterios.Criterio;
import domain.criterios.CriterioCategoria;
import domain.criterios.CriterioFechaAcontecimiento;
import domain.criterios.CriterioTitulo;
import domain.enumerados.Origen;
import domain.fuentes.Fuente;
import domain.fuentes.FuenteEstatica;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;

class ColeccionTest {

  private Hecho h1;
  private Hecho h2;
  private Hecho h3;
  private Hecho h4;
  private Hecho h5;

  private FuenteEstatica fuente;

  @BeforeEach
  public void init() {
    this.h1 = new Hecho(
        "Caída de aeronave impacta en Olavarría",
        "Grave caída de aeronave ocurrió en las inmediaciones de " +
            "Olavarría, Buenos Aires. El incidente provocó pánico entre " +
            "los residentes locales. Voluntarios de diversas organizaciones " +
            "se han sumado a las tareas de auxilio.",
        new Categoria("Caída de aeronave"),
        new Ubicacion(-36.86837, -60.343297),
        LocalDate.of(2001, 11, 29),
        Origen.CARGA_MANUAL
    );

    this.h2 = new Hecho(
        "Serio incidente: Accidente con maquinaria industrial en Chos Malal, Neuquén",
        "Un grave accidente con maquinaria industrial se registró en Chos Malal, " +
            "Neuquén. El incidente dejó a varios sectores sin comunicación. Voluntarios de " +
            "diversas organizaciones se han sumado a las tareas de auxilio.",
        new Categoria("Accidente con maquinaria industrial"),
        new Ubicacion(-37.345571,-70.241485),
        LocalDate.of(2001, 8, 16),
        Origen.CARGA_MANUAL
    );

    this.h3 = new Hecho(
        "Caída de aeronave impacta en Venado Tuerto, Santa Fe",
        "Grave caída de aeronave ocurrió en las inmediaciones " +
            "de Venado Tuerto, Santa Fe. El incidente destruyó viviendas " +
            "y dejó a familias evacuadas. Autoridades nacionales se han " +
            "puesto a disposición para brindar asistencia.",
        new Categoria("Caída de aeronave"),
        new Ubicacion(-33.768051,-61.921032),
        LocalDate.of(2008, 8, 8),
        Origen.CARGA_MANUAL
    );

    this.h4 = new Hecho(
        "Accidente en paso a nivel deja múltiples daños en Pehuajó, Buenos Aires",
        "Grave accidente en paso a nivel ocurrió en las inmediaciones de Pehuajó, " +
            "Buenos Aires. El incidente generó preocupación entre las autoridades provinciales. " +
            "El Ministerio de Desarrollo Social está brindando apoyo a los damnificados.",
        new Categoria("Accidente en paso a nivel"),
        new Ubicacion(-35.855811,-61.940589),
        LocalDate.of(2020, 1, 27),
        Origen.CARGA_MANUAL
    );

    this.h5 = new Hecho(
        "Devastador Derrumbe en obra en construcción afecta a Presidencia Roque Sáenz Peña",
        "Un grave derrumbe en obra en construcción se registró en Presidencia Roque Sáenz " +
            "Peña, Chaco. El incidente generó preocupación entre las autoridades provinciales. El " +
            "intendente local se ha trasladado al lugar para supervisar las operaciones.",
        new Categoria("Derrumbe en obra en construcción"),
        new Ubicacion(-35.855811,-60.458782),
        LocalDate.of(2016, 6, 4),
        Origen.CARGA_MANUAL
    );

    this.fuente = new FuenteEstatica("ruta.csv");
  }


  @Test
  @DisplayName("Tests de colecciones")
  public void crearColeccion(){

    // test escenario 1.1

    fuente.cargarHechos(h1, h2, h3, h4, h5);

    Coleccion coleccion = new Coleccion("Coleccion prueba", "Esto es una prueba", fuente);

    coleccion.filtrarHechos();

    assertTrue(coleccion.getHechosDeLaColeccion().containsAll(List.of(h1, h2, h3, h4, h5)));

    System.out.println("Titulo del hecho 1: " + h1.getTitulo());
    System.out.println("Titulo del hecho 2: " + h2.getTitulo());
    System.out.println("Titulo del hecho 3: " + h3.getTitulo());
    System.out.println("Titulo del hecho 4: " + h4.getTitulo());
    System.out.println("Titulo del hecho 5: " + h5.getTitulo());

    // test.escenario 1.2

    CriterioFechaAcontecimiento c1 = new CriterioFechaAcontecimiento(
        LocalDate.of(2000,1,1),
        LocalDate.of(2010,1,1));
    coleccion.agregarCriterios(c1);

    coleccion.filtrarHechos();

    assertTrue(coleccion.getHechosDeLaColeccion().containsAll(List.of(h1, h2, h3)));
    assertFalse(coleccion.getHechosDeLaColeccion().containsAll(List.of(h4, h5)));

    System.out.println("Titulo del hecho 1: " + h1.getTitulo());
    System.out.println("Titulo del hecho 2: " + h2.getTitulo());
    System.out.println("Titulo del hecho 3: " + h3.getTitulo());

    CriterioCategoria c2 = new CriterioCategoria(new Categoria("Caída de aeronave"));
    coleccion.agregarCriterios(c2);
    coleccion.filtrarHechos();

    assertTrue(coleccion.getHechosDeLaColeccion().containsAll(List.of(h1, h3)));
    assertFalse(coleccion.getHechosDeLaColeccion().containsAll(List.of(h2,h4,h5)));

    System.out.println("Titulo del hecho 1: " + h1.getTitulo());
    System.out.println("Titulo del hecho 3: " + h3.getTitulo());

    // test.escenario 1.3

    CriterioCategoria c3 = new CriterioCategoria(new Categoria("Caída de aeronave"));
    CriterioTitulo c4 = new CriterioTitulo("un título");
    coleccion.agregarCriterios(c3,c4);
    coleccion.filtrarHechos();

    assertTrue(coleccion.getHechosDeLaColeccion().isEmpty());
    assertFalse(coleccion.getHechosDeLaColeccion().containsAll(List.of(h1,h2,h3,h4,h5)));

    // test.escenario 1.4

    Etiqueta Olavarria = new Etiqueta("Olavarria");
    Etiqueta Grave = new Etiqueta("Grave");

    h1.agregarEtiqueta(Olavarria);
    h1.agregarEtiqueta(Grave);

    assertTrue(h1.getEtiquetas().containsAll(List.of(Olavarria , Grave)));
  }
}