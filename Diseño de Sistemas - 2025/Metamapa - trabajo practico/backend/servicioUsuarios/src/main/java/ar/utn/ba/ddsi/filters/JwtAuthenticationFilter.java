package ar.utn.ba.ddsi.filters;


import ar.utn.ba.ddsi.models.entities.Usuario;
import ar.utn.ba.ddsi.models.enums.Rol;
import ar.utn.ba.ddsi.models.repositories.IUsuariosRepository;
import ar.utn.ba.ddsi.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final IUsuariosRepository usuarioRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // 1. Verificación extra para evitar procesar "Bearer null"
        if (header != null && header.startsWith("Bearer ") && !header.endsWith("null")) {
            String token = header.substring(7);
            try {
                String username = JwtUtil.validarToken(token);

                Usuario user = usuarioRepo.findBymail(username)
                        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                Rol rolUsuario = user.getRol();
                var auth = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rolUsuario.toString()))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception e) {
                // 2. CAMBIO CRÍTICO: No retornar error. Solo loguear y continuar.
                // Si el token está mal, el usuario queda como "Anónimo".
                // SecurityConfig decidirá si el anónimo puede pasar o no.
                System.out.println("Token inválido o expirado, continuando como anónimo: " + e.getMessage());
                SecurityContextHolder.clearContext(); // Limpiar por seguridad
            }
        } else {
            System.out.println("No hay token de autorización válido o es null");
        }

        // 3. Siempre continuar la cadena
        filterChain.doFilter(request, response);
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/")) return true; // saltar filtro para web
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true; // CORS preflight
        return path.equals("/api/auth") || path.equals("/api/auth/refresh");
    }
    /* METODO ORIGINAL
    @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String path = request.getRequestURI();
            // No aplicar el filtro JWT solo a los endpoints públicos de autenticación
            return path.equals("/api/auth") || path.equals("/api/auth/refresh");
    }*/

}