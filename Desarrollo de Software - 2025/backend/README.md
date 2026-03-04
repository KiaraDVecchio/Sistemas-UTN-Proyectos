# TP DDSO
Birbinb

**Alumnos:**
- Benitez, Marianela
- Dieguez, Miranda
- Serral, Lucas
- Talotti, Valentino
- Vecchio, Kiara

---

**Flujo de git definido.**

En esta primera entrega comenzamos a aplicar formalmente el Git Flow que guiará el desarrollo de Birbnb. Hasta ahora:

- Creamos la rama **`avances_merge`**, donde subimos todo el trabajo que realizamos previamente a tener el repositorio grupal.
- Creamos la rama **`healthCheck`**, donde implementamos y subimos el endpoint `/health`.
- Finalmente, mergeamos ambos desarrollos en **`main`** para realizar esta primera entrega.

A partir de esta entrega, nuestro flujo de trabajo seguirá la siguiente estrategia de ramificación:

1. **Rama principal (****`main`****)**: siempre contiene el código estable y listo para producción. Cada entrega se mergea en esta rama una vez revisada.

2. **Ramas de funcionalidad (****`feature/…`****)**: cada nueva característica parte de `main` en una rama descriptiva, por ejemplo `feature/reservas`. En esta rama:

   - Desarrollamos la funcionalidad de forma aislada.
   - Realizamos commits claros.
   - Al completar la tarea, abrimos un Pull Request para fusionar en `main` tras la revisión.

3. **Ramas de corrección urgente (****`hotfix/…`****)**: si surge un error en producción, creamos directamente desde `main` una rama `hotfix/corregir-XXX`, corregimos y mergeamos rápido en `main`, seguido de un nuevo despliegue.

4. **Revisión e integración**:

   - Antes de cada merge, sincronizamos nuestra rama con `main` para resolver posibles conflictos.
   - Abrimos Pull Requests en GitHub, describiendo el propósito de los cambios, pruebas realizadas y solicitando al menos una revisión.
   - Una vez aprobados los cambios y pasados los tests, integramos la rama en `main`.

5. **Limpieza de ramas**: tras un merge, eliminamos la rama remota para mantener el repositorio ordenado.
