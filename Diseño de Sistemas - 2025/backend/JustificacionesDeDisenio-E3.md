## Modos de navegación

- Para modelar el comportamiento de los modos de navegación, definimos la interfaz `IModoDeNavegacion`, con dos implementaciones: `ModoIrrestricto` y `ModoCurado`.

  Esta decisión nos permite aplicar el patrón **Strategy**, ya que encapsulamos diferentes algoritmos de navegación en clases independientes, que comparten una interfaz común.

- Se utiliza además el patrón **Factory** para resolver dinámicamente qué modo se debe aplicar según el tipo seleccionado (`TipoDeModoNavegacion`). Esto favorece la **extensibilidad**, ya que, ante la necesidad de agregar un nuevo modo (por ejemplo, navegación solo por hechos verificados), no sería necesario modificar el código existente, sino simplemente agregar una nueva clase que implemente la interfaz y se registre automáticamente.
- El método `obtenerHechosPorColeccion()` de `ColeccionService` es el encargado de aplicar la navegación que se quiera. Obtiene los hechos de la colección, filtra los eliminados, y luego delega en el `modoDeNavegacionFactory` la resolución de la estrategia correspondiente. Finalmente, se aplica el filtro según el modo seleccionado (`curado` o `irrestricto`).
- Este diseño también promueve alta cohesión (cada clase tiene una única responsabilidad) y bajo acoplamiento (el `Service` desconoce las implementaciones concretas de los modos). Además la elección de usar interfaces permite que las estrategias sean intercambiables y testeables de forma aislada, contribuyendo a la mantenibilidad y la testabilidad del sistema.

## Algoritmos de Consenso

- Cada colección puede tener configurado un tipo de algoritmo (`MAYORIA_SIMPLE`, `CONSENSO_ABSOLUTO`, `MULTIPLES_MENCIONES`) a través de un `Enum` en el DTO correspondiente. Si no se define ninguno, se asume navegación irrestricta, mostrando todos los hechos disponibles.
- Para aplicar estos algoritmos, desarrollamos la interfaz `IAlgoritmoDeConsenso` y sus tres implementaciones concretas. Esto aplica nuevamente el patrón **Strategy**, ya que encapsulamos distintas formas de resolver el mismo problema de forma intercambiable.
- La clase `AlgoritmoDeConsensoFactory` se encarga de resolver dinámicamente cuál estrategia utilizar en base al `TipoAlgoritmoDeConsenso` de la colección. Esta lógica fue centralizada en el método `usoDeAlgoritmo()` de `ConsensoService`, que recibe una colección y el conjunto de hechos por fuente, y aplica la estrategia correspondiente. Esta estructura respeta el principio de **Open/Closed**, permitiendo agregar nuevas formas de consenso sin alterar clases existentes.
- Toda esta lógica se ejecuta de manera automática cada noche a las 3:00 AM, a través de un `Scheduler` (`ConsensoScheduler`) que llama a `consensoService.aplicarAlgoritmoDeConsenso()`.

## Exposición REST

Para garantizar la accesibilidad del sistema, se expusieron servicios REST en distintas capas:

- Desde `AdminService` y `AdminController`, se brinda la API para acciones administrativas (como gestionar solicitudes de eliminación o actualizar colecciones).
- El `AgregadorService` cumple el rol de API pública, accesible desde otras instancias de MetaMapa o aplicaciones externas.
