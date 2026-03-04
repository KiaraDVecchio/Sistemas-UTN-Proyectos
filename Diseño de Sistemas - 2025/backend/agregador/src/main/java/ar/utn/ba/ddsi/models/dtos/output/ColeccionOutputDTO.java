package ar.utn.ba.ddsi.models.dtos.output;

import ar.utn.ba.ddsi.models.dtos.input.CriterioInputDTO;
import ar.utn.ba.ddsi.models.dtos.input.CriterioInputDTO;
import ar.utn.ba.ddsi.models.entities.Coleccion;
import ar.utn.ba.ddsi.models.entities.Fuente;
import ar.utn.ba.ddsi.models.entities.criterios.*;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class ColeccionOutputDTO {
    private String titulo;
    private String descripcion;
    private String handle;
    private String algoritmoDeConsenso;
    private Integer cantVistas;

    private Set<Long> fuenteIds;
    private Set<Long> criterioIds;
    private Set<Long> hechoIds;


    private List<FuenteOutputDTO> fuentes = new ArrayList<>();
    private List<CriterioInputDTO> criterios = new ArrayList<>();

    public static ColeccionOutputDTO fromEntity(Coleccion c) {
        ColeccionOutputDTO dto = new ColeccionOutputDTO();
        dto.setTitulo(c.getTitulo());
        dto.setDescripcion(c.getDescripcion());
        dto.setHandle(c.getHandle());
        if(c.getAlgoritmoDeConsenso() != null) {
            dto.setAlgoritmoDeConsenso(c.getAlgoritmoDeConsenso().name());
        }
        dto.setCantVistas(c.getCantVistas());

        dto.setFuenteIds(c.getFuentes().stream().map(Fuente::getId).collect(Collectors.toSet()));
        dto.setHechoIds(c.getHechos().stream().map(h -> h.getId()).collect(Collectors.toSet()));
        dto.setCriterioIds(c.getCriterios().stream().map(cr -> 0L /* cr.getId() si lo tuvieras expuesto */).collect(Collectors.toSet()));

        if(c.getFuentes() != null) {
            dto.setFuentes(c.getFuentes().stream()
                    .map(FuenteOutputDTO::fromEntity)
                    .collect(Collectors.toList()));
        }

        if(c.getCriterios() != null) {
            dto.setCriterios(c.getCriterios().stream()
                    .map(ColeccionOutputDTO::convertirCriterioADTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private static CriterioInputDTO convertirCriterioADTO(ar.utn.ba.ddsi.models.entities.criterios.Criterio c) {
        CriterioInputDTO dto = new CriterioInputDTO();


        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (c instanceof CriterioTitulo) {

            dto.setTipoCriterio("TITULO");

            dto.setValorString(obtenerValorReflexion(c, "titulo"));

        } else if (c instanceof CriterioDescripcion) {
            dto.setTipoCriterio("DESCRIPCION");
            dto.setValorString(obtenerValorReflexion(c, "descripcion"));

        } else if (c instanceof CriterioCategoria) {
            dto.setTipoCriterio("CATEGORIA");
            CriterioCategoria cc = (CriterioCategoria) c;
            // Accedemos a la categoría dentro del criterio
            if(cc.getCategoria() != null) {
                dto.setValorString(cc.getCategoria().getNombre());
            }

        } else if (c instanceof CriterioOrigen) {
            dto.setTipoCriterio("ORIGEN");
            dto.setValorString(obtenerValorReflexion(c, "origen").toString());

        } else if (c instanceof CriterioFechaAcontecimiento) {
            dto.setTipoCriterio("FECHA_ACONTECIMIENTO");
            CriterioFechaAcontecimiento cf = (CriterioFechaAcontecimiento) c;
            if(cf.getDesde() != null) dto.setFechaDesde(cf.getDesde().format(fmt));
            if(cf.getHasta() != null) dto.setFechaHasta(cf.getHasta().format(fmt));

        } else if (c instanceof CriterioFechaCarga) {
            dto.setTipoCriterio("FECHA_CARGA");
            CriterioFechaCarga cf = (CriterioFechaCarga) c;
            if(cf.getDesde() != null) dto.setFechaDesde(cf.getDesde().format(fmt));
            if(cf.getHasta() != null) dto.setFechaHasta(cf.getHasta().format(fmt));

        } else if (c instanceof CriterioLugar) {
            dto.setTipoCriterio("LUGAR");
            CriterioLugar cl = (CriterioLugar) c;
            dto.setProvincia(cl.getProvinciaBuscada());
            dto.setRango(cl.getRangoMaximo());
            if(cl.getUbicacion() != null) {
                dto.setLatitud(cl.getUbicacion().getLatitud());
                dto.setLongitud(cl.getUbicacion().getLongitud());
            }
        }

        return dto;
    }

    private static String obtenerValorReflexion(Object obj, String campo) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(campo);
            field.setAccessible(true);
            Object value = field.get(obj);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }
}