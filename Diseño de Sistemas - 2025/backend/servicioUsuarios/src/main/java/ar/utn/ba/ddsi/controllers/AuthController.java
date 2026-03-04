package ar.utn.ba.ddsi.controllers;

import ar.utn.ba.ddsi.exceptions.NotFoundException;
import ar.utn.ba.ddsi.models.dtos.*;
import ar.utn.ba.ddsi.models.entities.Usuario;
import ar.utn.ba.ddsi.services.LoginService;
import ar.utn.ba.ddsi.services.SingupService;
import ar.utn.ba.ddsi.services.UsuarioService;
import ar.utn.ba.ddsi.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final LoginService loginService;
    private final SingupService singupService;
    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<AuthResponseDTO> loginApi(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            // Validación básica de credenciales
            if (username == null || username.trim().isEmpty() ||
                    password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Autenticar usuario usando el LoginService
            Usuario usuario = loginService.autenticarUsuario(username, password);
            // Generar tokens
            String accessToken = loginService.generarAccessToken(usuario);   // usa uid
            String refreshToken = loginService.generarRefreshToken(username);

            AuthResponseDTO response = AuthResponseDTO.builder() //generamos la respuesta
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

            log.info("El usuario {} está logueado. El token generado es {}", username, accessToken);

            return ResponseEntity.ok(response); //devolvemos la respuesta -> esto es lo que mandamos al front para guardar en sesion (llave de acceso)
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest request) {

        try {
            String username = JwtUtil.validarToken(request.getRefreshToken());

            // Validar que el token sea de tipo refresh
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(JwtUtil.getKey())
                    .build()
                    .parseClaimsJws(request.getRefreshToken()) //renuevo el acceso
                    .getBody();

            if (!"refresh".equals(claims.get("type"))) {
                return ResponseEntity.badRequest().build();
            }

            Usuario usuario = loginService.obtenerUsuarioPorMail(username);
            String newAccessToken = loginService.generarAccessToken(usuario);
            TokenResponse response = new TokenResponse(newAccessToken, request.getRefreshToken());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    //este lo hizo mas particular (mas simple) pero se puede hacer con los claimns recuperando la request igual que los anteriores
    @GetMapping("/user/roles-permisos")
    public ResponseEntity<UserRolesPermissionsDTO> getUserRolesAndPermissions(Authentication authentication) {
        try {
            String username = authentication.getName(); // authentication ya tiene el username, entonces lo sacamos de ahi en vez de hacer lo de claim
            UserRolesPermissionsDTO response = loginService.obtenerRolesYPermisosUsuario(username); //podemos obtener por nombre de usuario
            return ResponseEntity.ok(response);
        } catch (NotFoundException e) {
            log.error("Usuario no encontrado", e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error al obtener roles y permisos del usuario", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register (@RequestBody SingupDTO dto){
        try {
            singupService.registrarUsuario(dto);

            String username = dto.getEmail();

            Usuario usuario = loginService.obtenerUsuarioPorMail(username);
            String access = loginService.generarAccessToken(usuario);
            String refresh = loginService.generarRefreshToken(username);

            return ResponseEntity.ok(
                AuthResponseDTO.builder()
                    .accessToken(access)
                    .refreshToken(refresh)
                    .build());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // email duplicado
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/public/user/{id}")
    public ResponseEntity<ContribuyenteDTO> getUser(@PathVariable("id") Long id) {
        try {
            Usuario userEncontrado = usuarioService.findUser(id);
            ContribuyenteDTO response = new ContribuyenteDTO(userEncontrado);
            return ResponseEntity.ok(response);
        } catch (NotFoundException e) {
            log.error("Usuario no encontrado", e);
            return ResponseEntity.notFound().build();
        }
    }
}