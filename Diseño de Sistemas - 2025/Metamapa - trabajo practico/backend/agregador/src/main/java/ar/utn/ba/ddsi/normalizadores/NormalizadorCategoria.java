package ar.utn.ba.ddsi.normalizadores;

import ar.utn.ba.ddsi.models.entities.Categoria;
import ar.utn.ba.ddsi.models.repositories.ICategoriaRepository;
import ar.utn.ba.ddsi.services.IRaeService;
import jakarta.annotation.PostConstruct; // Import necesario
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class NormalizadorCategoria {

    private final ICategoriaRepository categoriaRepository;
    private final Normalizador normalizador;
    private final IRaeService diccionario;

    // Cache concurrente
    private final Map<String, Categoria> cacheCategorias = new ConcurrentHashMap<>();

    private static final Map<String, String> categoriaPorSinonimo = Map.ofEntries(
            Map.entry("fuego", "incendio"),
            Map.entry("fuego forestal", "incendio forestal")
    );

    public NormalizadorCategoria(ICategoriaRepository categoriaRepository, Normalizador normalizador, IRaeService diccionario) {
        this.categoriaRepository = categoriaRepository;
        this.normalizador = normalizador;
        this.diccionario = diccionario;
    }

    // --- MEJORA: PRE-CALENTAMIENTO DEL CACHÉ ---
    // Esto carga todas las categorías existentes al iniciar la app.
    // Evita los SELECTs individuales durante la carga del CSV.
    @PostConstruct
    public void inicializarCache() {
        try {
            var todas = categoriaRepository.findAll();
            for (Categoria c : todas) {
                String key = normalizador.normalizar(c.getNombre());
                if (key != null) {
                    cacheCategorias.put(key, c);
                }
            }
            System.out.println("Caché de categorías inicializado con " + todas.size() + " elementos.");
        } catch (Exception e) {
            System.err.println("No se pudo inicializar caché de categorías (puede que la tabla esté vacía): " + e.getMessage());
        }
    }

    public Categoria normalizarCategoria(Categoria categoria) {
        if (categoria == null) return null;

        String normalizada = normalizador.normalizar(categoria.getNombre());
        if (normalizada == null || normalizada.isBlank()) {
            throw new IllegalArgumentException("Nombre de categoria invalido");
        }

        // 1. CHECK CACHÉ (Ahora incluye las que ya estaban en BD desde el inicio)
        if (cacheCategorias.containsKey(normalizada)) {
            return cacheCategorias.get(normalizada);
        }

        // 2. Revisar diccionario estático local
        String candidato = categoriaPorSinonimo.getOrDefault(normalizada, normalizada);

        // Check rápido de nuevo por si el sinónimo ya estaba en caché
        String candidatoNorm = normalizador.normalizar(candidato);
        if (cacheCategorias.containsKey(candidatoNorm)) {
            return cacheCategorias.get(candidatoNorm);
        }

        // 3. Buscar en BD (Solo si falló el caché, que ya tiene todo lo de la BD)
        // Este paso ahora solo ocurrirá si es REALMENTE una categoría nueva
        var encontrada = categoriaRepository.findByNombreIgnoreCase(candidato);
        if (encontrada.isPresent()) {
            Categoria cat = encontrada.get();
            cacheCategorias.put(normalizada, cat);
            return cat;
        }

        // 4. Crear nueva
        try {
            Categoria nueva = categoriaRepository.save(new Categoria(candidato));
            cacheCategorias.put(normalizada, nueva);
            return nueva;
        } catch (DataIntegrityViolationException e) {
            Categoria existente = categoriaRepository.findByNombreIgnoreCase(candidato)
                    .orElseThrow(() -> new IllegalStateException("Error concurrente creando categoria", e));
            cacheCategorias.put(normalizada, existente);
            return existente;
        }
    }

    public void limpiarCache() {
        cacheCategorias.clear();
        inicializarCache(); // Recargar después de limpiar
    }
}