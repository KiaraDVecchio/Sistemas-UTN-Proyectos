package ar.utn.ba.ddsi.Metamapa.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
public class DashboardDTO {
    // ... tus campos anteriores (totales, mapas globales) ...
    private Long totalHechos;
    private Long hechosVerificados;
    private Long spamDetectado;
    private Double porcentajeSpam;
    private Map<String, Long> hechosPorCategoria;
    private Map<String, Long> hechosPorProvincia;
    private Map<Integer, Long> hechosPorHora;
    private List<DetalleCategoriaDTO> detallesPorCategoria;

    @Data
    public static class DetalleCategoriaDTO {
        private String categoria;
        private String provinciaTop;
        private Long cantidadEnProvincia;
        private String horaPico; // "14:00"
        private Long cantidadEnHora;
    }}