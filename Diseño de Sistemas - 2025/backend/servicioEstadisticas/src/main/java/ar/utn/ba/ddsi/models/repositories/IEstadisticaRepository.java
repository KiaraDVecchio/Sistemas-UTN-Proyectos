package ar.utn.ba.ddsi.models.repositories;

import ar.utn.ba.ddsi.models.entities.Estadistica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IEstadisticaRepository extends JpaRepository<Estadistica, Long> {

    Optional<Estadistica> findTopByTotalHechosIsNotNullOrderByFechaDeCalculoDesc();

    List<Estadistica> findAllByHechosPorProvinciaEnColeccion_ColeccionHandleOrderByFechaDeCalculoDesc(String handle);

    Optional<Estadistica> findTopByHechosPorProvinciaEnColeccion_ColeccionHandleOrderByFechaDeCalculoDesc(String handle);

    List<Estadistica> findAllByProvinciaTopPorCategoria_CategoriaOrderByFechaDeCalculoDesc(String categoriaNombre);

    Optional<Estadistica> findTopByProvinciaTopPorCategoria_CategoriaOrderByFechaDeCalculoDesc(String categoriaNombre);

    List<Estadistica> findAllByHorarioPicoPorCategoria_CategoriaOrderByFechaDeCalculoDesc(String categoriaNombre);

    Optional<Estadistica> findTopByHorarioPicoPorCategoria_CategoriaOrderByFechaDeCalculoDesc(String categoriaNombre);

    Optional<Estadistica> findTopByCategoriaTopGlobal_CategoriaGanadoraIsNotNullOrderByFechaDeCalculoDesc();

    Optional<Estadistica> findTopBySolicitudesEliminacionSpam_TotalIsNotNullOrderByFechaDeCalculoDesc();

    List<Estadistica> findAllByFechaDeCalculo(LocalDateTime fechaDeCalculo);

    Optional<Estadistica> findTopByOrderByFechaDeCalculoDesc();
}