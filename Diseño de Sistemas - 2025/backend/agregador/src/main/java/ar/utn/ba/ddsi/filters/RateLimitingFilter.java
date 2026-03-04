/*
package ar.utn.ba.ddsi.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;



@Component
@Order(1) //para darle prioridad
public class RateLimitingFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

  private final Map<String, Window> windows = new ConcurrentHashMap<>(); //key: ip+metodo+path, quién está haciendo la request y a qué endpoint
                                                                          // value: ventana de tiempo y cuantas request se hicieron
  private final int maxRequests;
  private final long windowMs;
  private final Set<String> excludedPaths = Set.of(
      "/actuator/health",
      "/actuator/info",
      "/static",
    "/api/hechos",
    "/api/hechos/import/hechos/csv"
  ); //paths que no queremos que rengan rate limit

  public RateLimitingFilter(
      @Value("${metamapa.rate-limit.max-requests:100}") int maxRequests,
      @Value("${metamapa.rate-limit.window-ms:60000}") long windowMs
  ) {
    this.maxRequests = maxRequests;
    this.windowMs = windowMs;
  }

  //metodo que se ejecuta por cada request http que pasa por este filtro
  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
      throws ServletException, IOException {

    String clientIp = extractClientIp(request); //obtenermos el ip del cliente
    String path = request.getRequestURI();
    String method = request.getMethod();

    String key = clientIp + ":" + method + ":" + path; //Ej: 127.0.0.1:GET:/api/hechos

    long now = System.currentTimeMillis();

    Window window = windows.compute(key, (k, w) -> {
      if (w == null || now - w.startTime >= windowMs) { //si no existe ventana o esta vencida se crea una nueva
        return new Window(now, new AtomicInteger(1));
      }
      w.counter.incrementAndGet(); //si no, se incrementa
      return w;
    });

    int count = window.counter.get();

    if (count > maxRequests) {
      log.warn("Rate limit excedido para IP {} en {} {} ({} requests en {} ms)",
          clientIp, method, path, count, windowMs);

      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType("application/json");
      response.getWriter().write("""
                    {
                      "error": "too_many_requests",
                      "message": "Se superó el límite de solicitudes permitido. Intenta de nuevo más tarde."
                    }
                    """);
      return; //devolvemos un 429 con un mensaje de error en formato json
    }

    filterChain.doFilter(request, response); //si esta dentro del limite que no haga nada
  }

  //funcion helper para sacar el ip del cliente
  private String extractClientIp(HttpServletRequest request) {
    String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      return xff.split(",")[0].trim(); // primera IP del header
    }
    return request.getRemoteAddr();
  }

  //para que no aplique rate limit en los excluded paths que declaramos al principio del archivo
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return excludedPaths.stream().anyMatch(path::startsWith);
  }

  private static class Window {
    final long startTime;
    final AtomicInteger counter;

    Window(long startTime, AtomicInteger counter) {
      this.startTime = startTime; //momento en que empieza la ventana
      this.counter = counter; //cuantas requests se hicieron dentro de esa ventana
    }
    //aclaracion: AtomicInteger permite incrementar el contador de forma segura con muchos hilos (lo pongo por las dudas, no parece hacer falta)
  }
}
*/
