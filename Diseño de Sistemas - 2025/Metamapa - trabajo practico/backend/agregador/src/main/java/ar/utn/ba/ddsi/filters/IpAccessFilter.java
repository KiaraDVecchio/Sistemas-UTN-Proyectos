package ar.utn.ba.ddsi.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Order(0) //Para que se ejecute antes del RateLimitingFilter
public class IpAccessFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(IpAccessFilter.class);

    private final Set<String> allowedIps;
    private final Set<String> blockedIps;

    public IpAccessFilter(
        @Value("${metamapa.security.allowed-ips:}") String allowedIps,
        @Value("${metamapa.security.blocked-ips:}") String blockedIps
    ) {
      this.allowedIps = parseList(allowedIps);
      this.blockedIps = parseList(blockedIps);
    } //parseList arma un Set<String> a partir de un string separado por comas


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {

      String clientIp = extractClientIp(request);

      if (!blockedIps.isEmpty() && blockedIps.contains(clientIp)) {
        log.warn("Request bloqueada: IP {} en blacklist", clientIp);
        deny(response, "IP bloqueada");
        return;
        //si la IP está en blocked, devuelve 403
      }

      if (!allowedIps.isEmpty() && !allowedIps.contains(clientIp)) {
        log.warn("Request bloqueada: IP {} no está en whitelist", clientIp);
        deny(response, "IP no autorizada");
        return;
      }//si hay whitelist definida y la IP no esta, tmb tira el 403

      filterChain.doFilter(request, response); //si pasa los filtros no hace nada
    }

    //funcion helper para traernos la ip del cliente: si existe el header X-Forwarded-For (como en la nube), usa esa IP y si no, usa getRemoteAddr()
    private String extractClientIp(HttpServletRequest request) {
      String xff = request.getHeader("X-Forwarded-For");
      if (xff != null && !xff.isBlank()) {
        return xff.split(",")[0].trim();
      }
      return request.getRemoteAddr();
    }

    //devuelve un 403 Forbidden con un mensaje en JSON y corta la ejecucion del request
    private void deny(HttpServletResponse response, String reason) throws IOException {
      response.setStatus(HttpStatus.FORBIDDEN.value());
      response.setContentType("application/json");
      response.getWriter().write("""
                {
                  "error": "forbidden",
                  "message": "%s"
                }
                """.formatted(reason));
    }

  //toma el string del properties con las IPs separadas por coma y lo convierte en un Set de IPs
    private Set<String> parseList(String raw) {
      if (raw == null || raw.isBlank()) return Set.of();
      return Arrays.stream(raw.split(","))
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .collect(Collectors.toSet());
    }

}
