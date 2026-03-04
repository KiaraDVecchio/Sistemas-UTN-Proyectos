package ar.utn.ba.ddsi.schedulers;

import ar.utn.ba.ddsi.services.IConsensoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ConsensoScheduler {
  @Autowired
  IConsensoService consensoService;
    private final String todosLosDias3AM = "0 0 3 * * *";
  private final long intervaloPrueba = 300000;

  @Scheduled(fixedDelay = intervaloPrueba ) //se ejecuta todos los dais a las 3AM (suponemos eso horario de baja acividad)
  public void ejecutarAlgoritmoDeConsenso() {
    try {
        System.out.println("SCHEDULER: Iniciando ejecución de algoritmos de consenso...");
      consensoService.aplicarAlgoritmoDeConsenso();
        System.out.println("SCHEDULER: Ejecución de algoritmos de consenso finalizada.");
    } catch (Exception e) {
      System.err.println("Error al ejecutar el algoritmo de consenso: " + e.getMessage());
    }
  }
}
