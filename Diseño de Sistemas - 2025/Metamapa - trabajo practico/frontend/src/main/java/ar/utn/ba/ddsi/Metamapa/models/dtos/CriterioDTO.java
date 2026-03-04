package ar.utn.ba.ddsi.Metamapa.models.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class CriterioDTO {
    private String tipoCriterio; // "TITULO", "CATEGORIA", "FECHA_CARGA", etc.

    private String valorString;

    private String fechaDesde;
    private String fechaHasta;

    private Double latitud;
    private Double longitud;
    private Integer rango;

    private String provincia;

    public CriterioDTO(String tipo, String valor) {
        this.tipoCriterio = tipo;
        this.valorString = valor;
    }
}