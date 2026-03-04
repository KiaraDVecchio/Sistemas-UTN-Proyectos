package ar.utn.ba.ddsi.schedulers;

import ar.utn.ba.ddsi.services.IEstadisticaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EstadisticaScheduler {

    @Autowired
    IEstadisticaService estadisticasService;

    @Scheduled(fixedDelay = 300000)
    public void runPeriodicRecalculation() {
        System.out.println("SCHEDULER ESTADÍSTICAS: Iniciando cálculo...");
        try {
            estadisticasService.recalcularEstadisticas();
            System.out.println("SCHEDULER ESTADÍSTICAS: Finalizado con éxito.");
        } catch (Exception e) {
            System.err.println("Error en scheduler estadísticas: " + e.getMessage());
        }
    }
}