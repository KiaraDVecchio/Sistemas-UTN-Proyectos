package ar.utn.ba.ddsi.services.impl;

import ar.utn.ba.ddsi.models.dtos.input.SolicitudInputDTO;
import ar.utn.ba.ddsi.models.entities.Hecho;
import ar.utn.ba.ddsi.models.entities.SolicitudDeEliminacion;
import ar.utn.ba.ddsi.models.entities.enumerados.EstadoSolicitud;
import ar.utn.ba.ddsi.models.repositories.IHechoRepository;
import ar.utn.ba.ddsi.models.repositories.ISolicitudRepository;
import ar.utn.ba.ddsi.services.IDetectorDeSpam;
import ar.utn.ba.ddsi.services.ISolicitudService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class SolicitudService implements ISolicitudService {

    private final ISolicitudRepository solicitudRepository;
    private final IDetectorDeSpam detectorDeSpam;
    private final IHechoRepository hechoRepository;

    public SolicitudService(ISolicitudRepository solicitudRepository, IDetectorDeSpam detectorDeSpam, IHechoRepository hechoRepository) {
        this.solicitudRepository = solicitudRepository;
        this.detectorDeSpam = detectorDeSpam;
        this.hechoRepository = hechoRepository;
    }


    @Override
    public SolicitudDeEliminacion RechazadoPorSpam(SolicitudDeEliminacion solicitud) {
        if (solicitud.getHecho() == null || detectorDeSpam.esSpam(solicitud.getJustificacion())) {
            solicitud.cambiarEstado(EstadoSolicitud.RECHAZADA);
            solicitud.setEsSpam(true);
            solicitud.setFechaAtencion(LocalDateTime.now());
        }
        return solicitudRepository.save(solicitud);
    }


    @Override
    public void aceptarSolicitud(SolicitudDeEliminacion solicitud) {
        Hecho hecho = solicitud.getHecho();
        solicitud.setFechaAtencion(LocalDateTime.now());
        solicitud.cambiarEstado(EstadoSolicitud.ACEPTADA);
        hecho.setFueEliminado(true);
        solicitudRepository.save(solicitud);
        hechoRepository.save(hecho);
    }

    @Override
    public void rechazarSolicitud(SolicitudDeEliminacion solicitud) {
        solicitud.setFechaAtencion(LocalDateTime.now());
        solicitud.cambiarEstado(EstadoSolicitud.RECHAZADA);
        solicitudRepository.save(solicitud);
    }

    @Override
    public SolicitudDeEliminacion crearSolicitud(SolicitudInputDTO solicitudDTO) {
        if (solicitudDTO == null) {
            throw new IllegalArgumentException("La solicitud no puede ser nula");
        }
        if (solicitudDTO.getHechoId() == null) {
            throw new IllegalArgumentException("hechoId es requerido");
        }

        if (solicitudDTO.getJustificacion() == null || solicitudDTO.getJustificacion().trim().length() < 500) {
            throw new IllegalArgumentException("La justificación debe tener al menos 500 caracteres");
        }

        Hecho hecho = hechoRepository.findById(solicitudDTO.getHechoId())
                .orElseThrow(() -> new IllegalArgumentException("No existe el hecho con id " + solicitudDTO.getHechoId()));


        SolicitudDeEliminacion entidad = new SolicitudDeEliminacion();
        entidad.setHecho(hecho);
        entidad.setJustificacion(solicitudDTO.getJustificacion().trim());
        entidad.setFechaEntrada(LocalDateTime.now());


        if (detectorDeSpam.esSpam(entidad.getJustificacion())) {
            entidad.setEsSpam(true);
            entidad.setEstado(EstadoSolicitud.RECHAZADA);
            entidad.setFechaAtencion(LocalDateTime.now());
        } else {
            entidad.setEsSpam(false);
            entidad.setEstado(EstadoSolicitud.PENDIENTE);
        }

        return solicitudRepository.save(entidad);
    }

    @Override
    public Long contarPorEstado(EstadoSolicitud estado) {
        return this.solicitudRepository.countByEstado(estado);
    }
}