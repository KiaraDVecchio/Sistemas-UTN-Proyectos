package ar.utn.ba.ddsi.normalizadores;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Component
public class NormalizadorFecha {

  private final List<DateTimeFormatter> formatosDiaPrimero;
  private final List<DateTimeFormatter> formatosMesPrimero;
  private final List<DateTimeFormatter> formatos2Digitos; // para yy -> 20yy

  public NormalizadorFecha() {
    Locale es = new Locale("es");
    formatosDiaPrimero = Arrays.asList(
        DateTimeFormatter.ISO_LOCAL_DATE,            // 2025-09-07
        DateTimeFormatter.ofPattern("dd/MM/uuuu"),   // 07/09/2025
        DateTimeFormatter.ofPattern("d/M/uuuu"),     // 7/9/2025
        DateTimeFormatter.ofPattern("dd-MM-uuuu"),   // 07-09-2025
        DateTimeFormatter.ofPattern("d-M-uuuu"),     // 7-9-2025
        DateTimeFormatter.ofPattern("d MMM uuuu", es),  // 7 sept 2025
        DateTimeFormatter.ofPattern("d MMMM uuuu", es)  // 7 septiembre 2025
    );

    formatosMesPrimero = Arrays.asList(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("MM/dd/uuuu"),
        DateTimeFormatter.ofPattern("M/d/uuuu"),
        DateTimeFormatter.ofPattern("MM-dd-uuuu")
    );

    formatos2Digitos = Arrays.asList(
        DateTimeFormatter.ofPattern("d/M/uu"), // 7/9/25
        DateTimeFormatter.ofPattern("dd/MM/uu"),
        DateTimeFormatter.ofPattern("M/d/uu"),
        DateTimeFormatter.ofPattern("MM/dd/uu")
    );
  }

  public LocalDate normalizarFecha(String fecha, boolean preferirDiaPrimero) {
    if (fecha == null) {
      throw new IllegalArgumentException("Fecha nula");
    }
    String s = fecha.trim();

    // 1) Intentar formatos preferidos (incluye ISO)
    LocalDate ld = tryFormattersList(s, preferirDiaPrimero ? formatosDiaPrimero : formatosMesPrimero);
    if (ld != null) return ld;

    // 2) Intentar la otra preferencia (fallback)
    ld = tryFormattersList(s, preferirDiaPrimero ? formatosMesPrimero : formatosDiaPrimero);
    if (ld != null) return ld;

    // 3) Intentar formatos con año de 2 dígitos -> los interpretamos como 2000 + yy
    ld = tryFormatters2Digits(s);
    if (ld != null) return ld;

    throw new IllegalArgumentException("Formato de fecha no reconocido: '" + fecha + "'");
  }

  // Overload práctico: por defecto day-first (Argentina)
  public LocalDate normalizarFecha(String fecha) {
    return normalizarFecha(fecha, true);
  }

  /* --------- Helpers --------- */

  private LocalDate tryFormattersList(String s, List<DateTimeFormatter> formatters) {
    for (DateTimeFormatter fmt : formatters) {
      try {
        return LocalDate.parse(s, fmt);
      } catch (DateTimeParseException ignored) {
      }
    }
    return null;
  }

  private LocalDate tryFormatters2Digits(String s) {
    for (DateTimeFormatter fmt : formatos2Digitos) {
      try {
        LocalDate parsed = LocalDate.parse(s, fmt);
        int year = parsed.getYear();
        if (year >= 0 && year < 100) {
          return parsed.withYear(2000 + year);
        }
        return parsed;
      } catch (DateTimeParseException ignored) {
      }
    }
    return null;
  }
}
