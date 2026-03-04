package ar.utn.ba.ddsi.models.repositories;

import ar.utn.ba.ddsi.models.entities.SolicitudDeEliminacion;
import ar.utn.ba.ddsi.models.entities.enumerados.EstadoSolicitud;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ISolicitudRepository extends JpaRepository<SolicitudDeEliminacion, Long> {
  //public SolicitudDeEliminacion save(SolicitudDeEliminacion solicitud);
  //public List<SolicitudDeEliminacion> findAll();
  //public List<SolicitudDeEliminacion> findByEstado(EstadoSolicitud estado);
  //public SolicitudDeEliminacion findById(Long id);
  public Long countByEstado(EstadoSolicitud estado);
    @Query("SELECT s FROM SolicitudDeEliminacion s WHERE " +
            "(:id IS NULL OR s.id = :id) AND " +
            "(:estado IS NULL OR s.estado = :estado) AND " +
            "(:fecha IS NULL OR CAST(s.fechaEntrada AS LocalDate) = :fecha)")
    Page<SolicitudDeEliminacion> buscarConFiltros(
            @Param("id") Long id,
            @Param("estado") EstadoSolicitud estado,
            @Param("fecha") LocalDate fecha,
            Pageable pageable
    );


}
