package ar.utn.ba.ddsi.services.impl;

import ar.utn.ba.ddsi.Exceptions.HechoCreacionException;
import ar.utn.ba.ddsi.models.dtos.input.HechoInputDTO;
import ar.utn.ba.ddsi.models.dtos.input.HechoInputEdicionDTO;
import ar.utn.ba.ddsi.models.dtos.output.ContribuyenteOutputDTO;
import ar.utn.ba.ddsi.models.dtos.output.UbicacionOutputDTO;
import ar.utn.ba.ddsi.models.entities.*;
import ar.utn.ba.ddsi.models.entities.enumerados.TipoSolicitud;
import ar.utn.ba.ddsi.models.repositories.ICategoriaRepository;
import ar.utn.ba.ddsi.models.repositories.IContenidoMultimediaRepository;
import ar.utn.ba.ddsi.services.ICategoriaService;
import ar.utn.ba.ddsi.services.IContenidoMultimediaService;
import ar.utn.ba.ddsi.services.IHechoService;
import ar.utn.ba.ddsi.services.ISolicitudService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ar.utn.ba.ddsi.models.dtos.output.HechoOutputDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import ar.utn.ba.ddsi.models.entities.enumerados.Origen;

import ar.utn.ba.ddsi.models.repositories.IHechoRepository;

import java.time.temporal.ChronoUnit;

import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class HechoService implements IHechoService {

  @Autowired
  private IHechoRepository hechoRepository;
  @Autowired
  private ICategoriaRepository categoriaRepository;
  @Autowired
  private ICategoriaService categoriaService;
  @Autowired
  private IContenidoMultimediaRepository contenidoMultimediaRepository;
  @Autowired
  private IContenidoMultimediaService contenidoMultimediaService;
  @Lazy
  @Autowired
  private ISolicitudService solicitudService;
  @Autowired
  private MinioService minioService;

  @Override
  public HechoOutputDTO crear(HechoInputDTO hechoInputDTO , MultipartFile[] multimedia) {
    // validaciones básicas
    if (hechoInputDTO.getTitulo() == null || hechoInputDTO.getTitulo().isBlank()) {
      throw new IllegalArgumentException("Título obligatorio");
    }
    if (hechoInputDTO.getDescripcion() == null || hechoInputDTO.getDescripcion().isBlank()) {
      throw new IllegalArgumentException("Descripción obligatoria");
    }
    if (hechoInputDTO.getCategoria() == null || hechoInputDTO.getCategoria().getNombre().isBlank()) {
      throw new IllegalArgumentException("Categoría obligatoria");
    }
    if (hechoInputDTO.getFechaAcontecimiento() == null) {
      throw new IllegalArgumentException("Fecha/hora del acontecimiento obligatoria");
    }
    try {
      Categoria categoria = this.categoriaService.findCategory(hechoInputDTO.getCategoria());
      Ubicacion ubicacion = new Ubicacion();
      ubicacion.setProvincia(hechoInputDTO.getCiudad().getProvincia());
      ubicacion.setLatitud(hechoInputDTO.getCiudad().getLatitud());
      ubicacion.setLongitud(hechoInputDTO.getCiudad().getLongitud());

      Hecho hecho = new Hecho(
          hechoInputDTO.getTitulo(),
          hechoInputDTO.getDescripcion(),
          categoria,
          ubicacion,
          hechoInputDTO.getFechaAcontecimiento(),
          Origen.CARGA_MANUAL);


      if (multimedia != null && multimedia.length > 0) {

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : multimedia) {
          if (file != null && !file.isEmpty()) {
            String url = minioService.upload(file);
            urls.add(url);
          }
        }

        List<ContenidoMultimedia> contenidos =
            mapearMultimedia(urls);

        hecho.setContenidosMultimedia(contenidos);
      }

      if(hechoInputDTO.getContribuyente() != null) {
          var contrDto = hechoInputDTO.getContribuyente();

          Contribuyente contr = new Contribuyente();
          contr.setIdContribuyente(contrDto.getIdContribuyente());
          contr.setNombre(contrDto.getNombre());
          contr.setApellido(contrDto.getApellido());
          contr.setFechaDeNacimiento(contrDto.getFechaDeNacimiento());

          hecho.setContribuyente(contr);
      }

      this.hechoRepository.save(hecho);
      this.solicitudService.create(hecho, TipoSolicitud.CREACION);

      return this.hechoOutputDTO(hecho);

    } catch (Exception e) {
      throw new HechoCreacionException("Error al crear el hecho: " + e.getMessage());
    }
  }

  @Override
  public HechoOutputDTO hechoOutputDTO(Hecho hecho) {
    HechoOutputDTO dto = new HechoOutputDTO();
    dto.setId(hecho.getId());
    dto.setTitulo(hecho.getTitulo());
    dto.setDescripcion(hecho.getDescripcion());
    dto.setCategoria(hecho.getCategoria().getNombre());
    dto.setUbicacion(ubicacionOutputDTO(hecho.getUbicacion()));
    dto.setFechaAcontecimiento(hecho.getFechaAcontecimiento());
    dto.setFechaCarga(hecho.getFechaCarga());
    dto.setFueEliminado(hecho.getFueEliminado());

    //dto.setOrigen(hecho.getOrigen());       // extrae el id de cada etiqueta y los junta en un Set<Integer>
    // dto.setIdEtiquetas(hecho.getEtiquetas().stream().map(Etiqueta::getId).collect(Collectors.toSet()));
    //dto.setContribuyente(contribuyenteOutputDTO(hecho.getContribuyente()));
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode extras = mapper.createObjectNode();

    if (hecho.getFechaActualizacion() != null) {
      extras.put("fechaActualizacion", hecho.getFechaActualizacion().toString());
    }

    if (hecho.getContribuyente() != null) {
      ObjectNode c = mapper.createObjectNode();
      c.put("id", hecho.getContribuyente().getIdContribuyente());
      c.put("nombre", hecho.getContribuyente().getNombre());
      c.put("apellido", hecho.getContribuyente().getApellido());
      if (hecho.getContribuyente().getFechaDeNacimiento() != null) {
        c.put("fechaDeNacimiento", hecho.getContribuyente().getFechaDeNacimiento().toString());
      }
      extras.set("contribuyente", c);
    }
      boolean pendiente = solicitudService.existeSolicitudPendiente(hecho.getId(), TipoSolicitud.EDICION);
      dto.setTieneEdicionPendiente(pendiente);
    dto.setParticulares(extras);
    dto.setEtiquetas(hecho.getEtiquetas().stream().map(Etiqueta::getNombre).collect(Collectors.toSet()));
    dto.setPathContenidoMultimedia(hecho.getContenidosMultimedia().stream().map(ContenidoMultimedia::getPath).collect(Collectors.toList()));
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
  public ContribuyenteOutputDTO contribuyenteOutputDTO(Contribuyente contribuyente) {
    ContribuyenteOutputDTO dto = new ContribuyenteOutputDTO();
    dto.setId(contribuyente.getIdContribuyente());
    dto.setNombre(contribuyente.getNombre());
    dto.setApellido(contribuyente.getApellido());
    dto.setFechaDeNacimiento(contribuyente.getFechaDeNacimiento());
    return dto;
  }

  @Override
  public void eliminar(Long id) {
    var hecho = this.hechoRepository.findById(id).orElse(null);
    if (hecho == null) {
      throw new NoSuchElementException("No se puede eliminar. Hecho no encontrado con ID: " + id);
    }
      hecho.setFueEliminado(true);
      this.hechoRepository.save(hecho);
  }

  @Override
  public boolean puedeEditar(Long id1 , Long id2, LocalDate fecha) {

    boolean esMismoUsuario = Objects.equals(id1, id2);
    boolean estaDentroDelPlazo = ChronoUnit.DAYS.between(fecha, LocalDate.now()) <= 7;

    return  esMismoUsuario && estaDentroDelPlazo;
  }

  @Override
  public HechoOutputDTO permisoDeEdicion(Long idEditor, Long idHecho) {
    Hecho hecho = this.hechoRepository.findById(idHecho).orElse(null);
    if (puedeEditar(idEditor, hecho.getContribuyente().getIdContribuyente(), hecho.getFechaCarga())) {
      return this.hechoOutputDTO(hecho);
    } else {
      throw new IllegalStateException("El plazo de edicion ha expirado");
    }
  }

  /*@Override
  public HechoOutputDTO edicion(Long idEditor, HechoInputDTO hechoInputDTO, Long idHecho) {
    Hecho hecho = this.hechoRepository.findById(idHecho).orElse(null);
    HechoEstadoPrevio estadoPrevio = new HechoEstadoPrevio(hecho);

    Categoria categoria = this.categoriaService.findCategory(hechoInputDTO.getCategoria());

    this.actualizarHecho(hecho,
        hechoInputDTO.getTitulo(),
        hechoInputDTO.getDescripcion(),
        categoria, hechoInputDTO.getCiudad(),
        hechoInputDTO.getFechaAcontecimiento());

    reemplazarArchivoMultimedia(hecho, hechoInputDTO.getPathsMultimedia());

    hecho.setEstadoPrevio(estadoPrevio);

    this.solicitudService.create(hecho, TipoSolicitud.EDICION);
    this.hechoRepository.save(hecho);

    return this.hechoOutputDTO(hecho);
  }*/ //TODO borrar cuando funcione el nuevo editar

  @Override
  @Transactional
  public HechoOutputDTO edicion(Long idEditor, HechoInputEdicionDTO dto, Long idHecho, MultipartFile[] multimedia, boolean replaceMedia, List<String> deleteExisting) {
    Hecho hecho = hechoRepository.findById(idHecho).orElseThrow(() -> new RuntimeException("Hecho no encontrado"));

    HechoEstadoPrevio estadoPrevio = new HechoEstadoPrevio(hecho);

    Categoria categoria = categoriaService.findCategory(dto.getCategoria());

    actualizarHecho(hecho, dto.getTitulo(), dto.getDescripcion(), categoria, dto.getCiudad(), dto.getFechaAcontecimiento());

    actualizarMultimedia(hecho, multimedia, replaceMedia, deleteExisting);

    hecho.setEstadoPrevio(estadoPrevio);

    solicitudService.create(hecho, TipoSolicitud.EDICION);

    hechoRepository.save(hecho);

    return hechoOutputDTO(hecho);
  }


  @Override
  public void creacionRechazada(Hecho hecho){
    hecho.setFueEliminado(true);
    this.hechoRepository.save(hecho);
  }

  @Override
  @Transactional
  public void edicionRechazada(Hecho hecho){
    HechoEstadoPrevio estadoPrevio = hecho.getEstadoPrevio();

    this.actualizarHecho(hecho,
        estadoPrevio.getTitulo(),
        estadoPrevio.getDescripcion(),
        estadoPrevio.getCategoria(),
        estadoPrevio.getUbicacion(),
        estadoPrevio.getFechaAcontecimiento());

    reemplazarArchivoMultimedia(hecho, getPathsFromMultimedia(estadoPrevio.getContenidosMultimedia()));

    hecho.setEstadoPrevio(null);

    this.hechoRepository.save(hecho);
  }

  private List<String> getPathsFromMultimedia(List<ContenidoMultimedia> lista) {
    if(lista == null) return Collections.emptyList();
    return lista.stream().map(ContenidoMultimedia::getPath).toList();
  }

  private void reemplazarArchivoMultimedia(Hecho hecho, List<String> nuevosPaths) {
    if (nuevosPaths == null) return;

    List<ContenidoMultimedia> nuevosObjetos = mapearMultimedia(nuevosPaths);

    hecho.actualizarContenidosMultimedia(nuevosObjetos);
  }

  private List<ContenidoMultimedia> mapearMultimedia(List<String> paths) {
    if (paths == null) return Collections.emptyList();
    return contenidoMultimediaService.mapeosMultimedia(paths);
  }

  @Override
  public List<HechoOutputDTO> buscarTodos() {
    return this.hechoRepository
        .findAll()
        .stream()
        .map(this::hechoOutputDTO)
        .toList();

  }

  @Override
  public void actualizarHecho(Hecho hecho,
                              String titulo,
                              String descripcion,
                              Categoria categoria,
                              Ubicacion ubicacion,
                              LocalDateTime fechaAcontecimiento) {

    if (titulo != null && !titulo.isBlank()) {
      hecho.setTitulo(titulo);
    }
    if (descripcion != null && !descripcion.isBlank()) {
      hecho.setDescripcion(descripcion);
    }
    if (categoria != null) {            // si viene null, no la toco
      hecho.setCategoria(categoria);
    }
    if (ubicacion != null) {           // idem
      hecho.setUbicacion(ubicacion);
    }
    if (fechaAcontecimiento != null) { // idem
      hecho.setFechaAcontecimiento(fechaAcontecimiento);
    }

    hecho.setFechaActualizacion(LocalDate.now());
  }

  private void actualizarMultimedia(
      Hecho hecho,
      MultipartFile[] multimedia,
      boolean replaceMedia,
      List<String> deleteExisting
  ) {
    List<ContenidoMultimedia> listaCalculada = new ArrayList<>(hecho.getContenidosMultimedia());

    if (replaceMedia) {
      listaCalculada.clear();
    }

    if (!replaceMedia && deleteExisting != null && !deleteExisting.isEmpty()) {
      Set<String> pathsAEliminar = new HashSet<>(deleteExisting);
      listaCalculada.removeIf(c -> pathsAEliminar.contains(c.getPath()));
    }

    if (multimedia != null && multimedia.length > 0) {
      List<String> nuevasUrls = new ArrayList<>();
      for (MultipartFile file : multimedia) {
        if (file != null && !file.isEmpty()) {
          String path = minioService.upload(file);
          nuevasUrls.add(path);
        }
      }
      List<ContenidoMultimedia> nuevos = mapearMultimedia(nuevasUrls);
      listaCalculada.addAll(nuevos);
    }

    hecho.actualizarContenidosMultimedia(listaCalculada);
  }



}
