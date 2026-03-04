package ar.utn.ba.ddsi.Metamapa.services;

import ar.utn.ba.ddsi.Metamapa.exceptions.NotFoundException;
import ar.utn.ba.ddsi.Metamapa.models.dtos.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class MetaMapaApiService {

    private static final Logger log = LoggerFactory.getLogger(MetaMapaApiService.class);
    private final WebClient webClient;
    private final WebApiCallerService webApiCallerService;
    private final String authServiceUrl;
    private final String baseAdminUrl;
    private final String basePublicUrl;
    private final String baseEstaticaUrl;

    @Autowired
    public MetaMapaApiService(
            //backend.api.base-url-agregador=http://localhost:8083/api/public
            WebApiCallerService webApiCallerService,
            @Value("${auth.service.url}") String authServiceUrl,
            @Value("${backend.api.base-url}") String baseAdminUrl,
            @Value("${backend.api.base-url-agregador}") String basePublicUrl,
            @Value("${backend.api.base-url-estatica}") String baseEstaticaUrl) {
        this.webClient = WebClient.builder().build();
        this.webApiCallerService = webApiCallerService;
        this.authServiceUrl = authServiceUrl;
        this.baseAdminUrl = baseAdminUrl;
        this.basePublicUrl = basePublicUrl;
        this.baseEstaticaUrl = baseEstaticaUrl;
    }

    public AuthResponseDTO login(String username, String password) {
        try {
            AuthResponseDTO response = webClient
                    .post()
                    .uri(authServiceUrl + "/auth")
                    .bodyValue(Map.of(
                            "username", username,
                            "password", password
                    ))                    .retrieve()
                    .bodyToMono(AuthResponseDTO.class)
                    .block();
            return response;
        } catch (WebClientResponseException e) {
            log.error(e.getMessage());
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                // Login fallido - credenciales incorrectas
                return null;
            }
            // Otros errores HTTP
            throw new RuntimeException("Error en el servicio de autenticación: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error de conexión con el servicio de autenticación: " + e.getMessage(), e);
        }
    }


    public AuthResponseDTO signupAndGetTokens(RegisterRequestDTO dto) {
        return webClient.post()
                .uri(authServiceUrl + "/auth/register")
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(AuthResponseDTO.class)
                .block();
    }


    public RolesPermisosDTO getRolesPermisos(String accessToken) {
        try {
            RolesPermisosDTO response = webApiCallerService.getWithAuth(
                    authServiceUrl + "/auth/user/roles-permisos",
                    accessToken,
                    RolesPermisosDTO.class
            );
            return response;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("Error al obtener roles y permisos: " + e.getMessage(), e);
        }
    }

    public ResumenDTO getPanelDeControl() {
        String accesToken = webApiCallerService.getAccessTokenFromSession();
        return webApiCallerService.getWithAuth(
                baseAdminUrl + "/resumen", //el metodo que voy a agregar al back
                accesToken,
                ResumenDTO.class
        );
    }

    public List<SolicitudDTO> getSolicitudes() {

        String accessToken = webApiCallerService.getAccessTokenFromSession();

        // Pedimos un array de DTOs y lo convertimos a List
        SolicitudDTO[] arr = webApiCallerService.getWithAuth(
                basePublicUrl + "/solicitudes",
                accessToken,
                SolicitudDTO[].class
        );

        if (arr == null || arr.length == 0) {
            return Collections.emptyList();
        }

        return Arrays.asList(arr);
    }

    public List<HechoDTO> getHechos() {

        String accessToken = webApiCallerService.getAccessTokenFromSession();

        // Pedimos un array de DTOs y lo convertimos a List
        HechoDTO[] arr = webApiCallerService.getWithAuth(
                basePublicUrl + "/hechos",
                accessToken,
                HechoDTO[].class
        );

        if (arr == null || arr.length == 0) {
            return Collections.emptyList();
        }

        return Arrays.asList(arr);
    }

    public ColeccionDTO crearColeccion(ColeccionDTO coleccion) {
        return webApiCallerService.post(
                baseAdminUrl + "/colecciones",
                coleccion,
                ColeccionDTO.class
        );
    }

    public ColeccionDTO actualizarColeccion(String handle, ColeccionDTO coleccion){
        return webApiCallerService.put(
                baseAdminUrl + "/colecciones/" + handle,
                coleccion,
                ColeccionDTO.class
        );
    }

    public InformeDeResultadosDTO importarHechosCsv(MultipartFile archivo){
        MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("archivo", archivo.getResource())
                .filename(archivo.getOriginalFilename() != null ? archivo.getOriginalFilename() : "import.csv")
                .contentType(MediaType.parseMediaType("text/csv"));
        final String PART_NAME = "archivo";
        return webApiCallerService.postMultipart(
                baseEstaticaUrl + "/import/hechos/csv",
                PART_NAME,
                archivo,
                InformeDeResultadosDTO.class

        );
    }

    public HechoDTO getHechoPorId(Long id){
        String accessToken = webApiCallerService.getAccessTokenFromSession();
        return webApiCallerService.getWithAuth(
                basePublicUrl +"/hechos/"+ id,
                accessToken,
                HechoDTO.class
        );
    }

    public List<CategoriaDTO> getCategorias() {

        String accessToken = webApiCallerService.getAccessTokenFromSession();

        // Pedimos un array de DTOs y lo convertimos a List
        CategoriaDTO[] arr = webApiCallerService.getWithAuth(
                basePublicUrl + "/categorias",
                accessToken,
                CategoriaDTO[].class
        );
        if (arr == null || arr.length == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(arr);
    }

    /*Devuelve el id de usuario (uid) guardado en el accessToken JWT,
     o null si no se puede decodificar. */
    public Long getUsuarioIdFromAccessToken() {
        String token = webApiCallerService.getAccessTokenFromSession();
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            // JWT = header.payload.signature
            String[] partes = token.split("\\.");
            if (partes.length < 2) return null;

            String payload = partes[1];

            // Base64 URL-safe decode
            byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
            String json = new String(decodedBytes, StandardCharsets.UTF_8);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json);

            JsonNode uidNode = node.get("uid");
            if (uidNode != null && !uidNode.isNull()) {
                return uidNode.asLong();
            }
            return null;
        } catch (Exception e) {
            // log.error("Error al decodificar uid del token", e);
            return null;
        }
    }

    public List<FuenteDTO> getFuentes() {
        String accessToken = webApiCallerService.getAccessTokenFromSession();

        FuenteDTO[] arr = webApiCallerService.getWithAuth(
            basePublicUrl + "/fuentes",
            accessToken,
            FuenteDTO[].class
        );

        return arr != null ? Arrays.asList(arr) : List.of();
    }

    public ContribuyenteDTO getContribuyente(Long id) {
        String token = webApiCallerService.getAccessTokenFromSession();
        String url = authServiceUrl + "/auth/public/user/" + id;

        return webClient.get()
            .uri(url)
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .bodyToMono(ContribuyenteDTO.class)
            .block();
    }

    public Long getIdLocalPorIdFuente(Long idEnFuente) {
        try {
            return webClient.get()
                .uri(basePublicUrl + "/hechos/id-por-fuente/{id}", idEnFuente)
                .retrieve()
                .bodyToMono(Long.class)
                .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new NotFoundException("El hecho con ID de fuente " + idEnFuente + " no se encuentra sincronizado en el mapa aún.");
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar el ID local del hecho", e);
        }
    }
}




