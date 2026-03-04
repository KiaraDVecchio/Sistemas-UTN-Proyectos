package ar.utn.ba.ddsi.services.impl;

import ar.utn.ba.ddsi.models.dtos.output.HechoOutputEstaticaDTO;
import ar.utn.ba.ddsi.models.dtos.output.RutaOutputDTO;
import ar.utn.ba.ddsi.models.entities.*;
import ar.utn.ba.ddsi.models.dtos.output.UbicacionOutputDTO;
import ar.utn.ba.ddsi.models.entities.enumerados.Origen;
import ar.utn.ba.ddsi.models.repositories.ICategoriaRepository;
import ar.utn.ba.ddsi.models.repositories.IHechoRepository;
import ar.utn.ba.ddsi.models.repositories.IRutasRepository;
import ar.utn.ba.ddsi.services.IFuenteEstaticaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FuenteEstaticaService implements IFuenteEstaticaService {

  @Autowired
  public IHechoRepository hechoRepository;
  @Autowired
  public IRutasRepository rutasRepository;
  @Autowired
  public ICategoriaRepository categoriaRepository;

  private static final DateTimeFormatter FECHA_CSV = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final List<String> HEADER_ESPERADO = List.of(
      "Título","Descripción","Categoría","Latitud","Longitud","Fecha del hecho"
  );

  @Override
  public void leerHechos(Long idRuta) {
    Ruta ruta = rutasRepository.findById(idRuta).orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada: " + idRuta));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");


    try (CSVReader reader = new CSVReader(
        new BufferedReader(new InputStreamReader(new FileInputStream(ruta.getPath()), StandardCharsets.UTF_8)))) {

      String[] fila;
      reader.readNext(); // header

      while ((fila = reader.readNext()) != null) {
        if (fila.length < 6) {
          System.err.println("Fila con columnas insuficientes: " + Arrays.toString(fila));
          continue;
        }

        String titulo      = normalizarString(fila[0]);
        String descripcion = normalizarString(fila[1]);
        String catNombre   = normalizarString(fila[2]);
        String latStr      = normalizarNumero(fila[3]);
        String lonStr      = normalizarNumero(fila[4]);
        String fechaStr    = normalizarString(fila[5]);

        double lat = Double.parseDouble(latStr);
        double lon = Double.parseDouble(lonStr);
        LocalDate fechaAcontecimiento = LocalDate.parse(fechaStr, formatter);

        Categoria categoria = new Categoria(catNombre);
        categoriaRepository.save(categoria);
        Ubicacion ubicacion = new Ubicacion(lat, lon);

        Hecho hechoACargar = new Hecho(
            titulo, descripcion, categoria, ubicacion, fechaAcontecimiento,
            Origen.PROVENIENTE_DE_DATASET
        );

        hechoACargar.setRuta(ruta);
        hechoRepository.save(hechoACargar);
      }

    } catch (IOException | CsvValidationException e) {
      throw new RuntimeException("No se pudo leer el CSV (" + ruta.getPath() + "): " + e.getMessage(), e);
    }
  }

  private static String normalizarString(String s) {
    return s == null ? null : s.replace('\u00A0',' ').trim();
  }

  private static String normalizarNumero(String s) {
    if (s == null) return null;
    return s.replace('\u2212', '-').replace('\u00A0',' ').trim();
  }

  @Override
  public void leerTodosLosArchivos() {
    for (Ruta ruta : rutasRepository.findAll()) {
      try {
        System.out.println(ruta.getIdRuta());
        System.out.println(ruta.getPath());
        this.leerHechos(ruta.getIdRuta());
      } catch (Exception e) {
        throw new RuntimeException("No hay archivos para leer");
      }
    }
  }

  @Override
  public List<HechoOutputEstaticaDTO> buscarTodos() {
    return this.hechoRepository
        .findAll()
        .stream()
        .map(this::hechoOutputEstaticaDTO)
        .toList();
  }

  @Override
  public HechoOutputEstaticaDTO hechoOutputEstaticaDTO(Hecho hecho) {
    HechoOutputEstaticaDTO dto = new HechoOutputEstaticaDTO();
    dto.setId(hecho.getId());
    dto.setTitulo(hecho.getTitulo());
    dto.setDescripcion(hecho.getDescripcion());
    dto.setCategoria(hecho.getCategoria().getNombre());
    dto.setUbicacion(ubicacionOutputDTO(hecho.getUbicacion()));
    dto.setFechaAcontecimiento(hecho.getFechaAcontecimiento().atStartOfDay());
    dto.setFechaCarga(hecho.getFechaCarga());

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode extras = mapper.createObjectNode();
    if (hecho.getRuta() != null && hecho.getRuta().getNombre() != null) {
      extras.put("rutaNombre", hecho.getRuta().getNombre());
    }
    dto.setParticulares(extras);

    dto.setEtiquetas(hecho.getEtiquetas().stream().map(Etiqueta::getNombre).collect(Collectors.toSet()));
    return dto;
  }

  @Override
  public UbicacionOutputDTO ubicacionOutputDTO(Ubicacion ubicacion) {
    UbicacionOutputDTO dto = new UbicacionOutputDTO();
    dto.setLatitud(ubicacion.getLatitud());
    dto.setLongitud(ubicacion.getLongitud());
    return dto;
  }

  @Override
  public RutaOutputDTO crearRuta(String nombre, String path){
    Ruta ruta = new Ruta();
    ruta.setNombre(nombre);
    ruta.setPath(path);
    rutasRepository.save(ruta);
    System.out.println(ruta.getIdRuta());
    return rutaOutputDTO(ruta);
  }

  public RutaOutputDTO rutaOutputDTO(Ruta ruta) {
    RutaOutputDTO rutaOutput = new RutaOutputDTO();
    rutaOutput.setIdRuta(ruta.getIdRuta());
    rutaOutput.setNombre(ruta.getNombre());
    rutaOutput.setPath(ruta.getPath());
    return rutaOutput;
  }

  @Override
  public InformeDeResultados procesarCsv(MultipartFile file) {
    long tiempo0 = System.currentTimeMillis();

    final String nombreOriginal = (file.getOriginalFilename() != null) ? file.getOriginalFilename() : "archivo.csv";

    final Path destino = guardarArchivo(file);

    Ruta rutaEntity = new Ruta();
    rutaEntity.setNombre(nombreOriginal);
    rutaEntity.setPath(destino.toString());
    rutasRepository.save(rutaEntity);

    final int BATCH_SIZE = 1000;
    long total = 0;
    long guardados = 0;
    long ignorados = 0;

    Map<String, Categoria> categoriaCache = new HashMap<>();
    categoriaRepository.findAll().forEach(c -> categoriaCache.put(c.getNombre().trim().toUpperCase(), c));

    List<Hecho> batch = new ArrayList<>(BATCH_SIZE);

    try (BufferedReader br = Files.newBufferedReader(destino, StandardCharsets.UTF_8);
         CSVReader reader = new CSVReaderBuilder(br).build()) {

      reader.readNext();

      String[] fila;
      while ((fila = reader.readNext()) != null) {
        total++;

        try {
          Hecho h = leerArchivo(fila, categoriaCache);

          h.setRuta(rutaEntity);

          batch.add(h);

        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
          System.err.println("Error en fila " + total + ": " + e.getMessage());
          ignorados++;
          continue;
        }

        if (batch.size() >= BATCH_SIZE) {
          hechoRepository.saveAll(batch);
          hechoRepository.flush();
          batch.clear();
          guardados += BATCH_SIZE;
        }
      }
    } catch (IOException | CsvValidationException e) {
      throw new IllegalStateException("Error grave leyendo el archivo CSV: " + e.getMessage(), e);
    }

    if (!batch.isEmpty()) {
      hechoRepository.saveAll(batch);
      hechoRepository.flush();
      guardados += batch.size();
    }

    long tiempo = System.currentTimeMillis() - tiempo0;

    System.out.println("Proceso finalizado. Total: " + total + ", Guardados: " + guardados + ", Ignorados: " + ignorados);

    return InformeDeResultados.builder()
        .nombreOriginal(nombreOriginal)
        .guardadoComo(destino.toString().replace('\\', '/'))
        .hechosTotales(total)
        .guardadosTotales(guardados)
        .tiempoTardado(tiempo)
        .build();
  }

  private Path guardarArchivo(MultipartFile file) {
    try {
      Path dir = Paths.get("imports");
      Files.createDirectories(dir);

      String nombre = UUID.randomUUID() + ".csv";
      Path destino = dir.resolve(nombre);

      try (InputStream in = file.getInputStream()) {
        Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
      }
      return destino;
    } catch (IOException e) {
      throw new IllegalStateException("No pude guardar el archivo en la carpeta de imports", e);
    }
  }

  private Hecho leerArchivo(String[] fila, Map<String, Categoria> categoriaCache) {
    if (fila == null || fila.length < 6) {
      throw new IllegalArgumentException("Fila inválida: se esperaban 6 columnas");
    }

    String titulo           = safeTrim(fila[0]);
    String descripcion      = safeTrim(fila[1]);
    String categoriaNombre  = safeTrim(fila[2]);
    String latStr           = safeTrim(fila[3]);
    String lonStr           = safeTrim(fila[4]);
    String fechaStr         = safeTrim(fila[5]);

    if (isBlank(titulo) || isBlank(descripcion) || isBlank(categoriaNombre)
        || isBlank(latStr) || isBlank(lonStr) || isBlank(fechaStr)) {
      throw new IllegalArgumentException("Fila con campos requeridos vacíos");
    }

    final double latitud;
    final double longitud;
    final LocalDate fecha;

    try {
      latitud = Double.parseDouble(latStr);
      longitud = Double.parseDouble(lonStr);
    } catch (NumberFormatException nfe) {
      throw new IllegalArgumentException("Coordenadas inválidas (no numéricas)");
    }

    try {
      fecha = LocalDate.parse(fechaStr, FECHA_CSV);
    } catch (Exception pe) {
      throw new IllegalArgumentException("Fecha inválida (formato esperado dd/MM/yyyy)");
    }

    String key = categoriaNombre.trim().toUpperCase();
    Categoria categoria = categoriaCache.get(key);
    if (categoria == null) {
      categoria = categoriaRepository.save(new Categoria(categoriaNombre));
      categoriaCache.put(key, categoria);
    }

    Ubicacion ubicacion = new Ubicacion(latitud, longitud);

    return new Hecho(
        titulo,
        descripcion,
        categoria,
        ubicacion,
        fecha,
        Origen.PROVENIENTE_DE_DATASET
    );
  }

  private static String safeTrim(String s) {
    return (s == null) ? null : s.replace('\u00A0',' ').trim();
  }
  private static boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }

}
