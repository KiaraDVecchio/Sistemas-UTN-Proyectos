package ar.utn.ba.ddsi.models.entities;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.Period;

@Embeddable
@NoArgsConstructor
@Getter
@Setter
public class Contribuyente {
    @JsonAlias("id")
    private Long idContribuyente;
    private String nombre;
    private String apellido;
    private LocalDate fechaDeNacimiento;

    public Contribuyente(Long idContribuyente, String nombre, LocalDate fechaDeNacimiento, String apellido) {
        this.idContribuyente = idContribuyente;
        this.nombre = nombre;
        this.fechaDeNacimiento = fechaDeNacimiento;
        this.apellido = apellido;
    }

    public Integer getEdad(){
        LocalDate hoy = LocalDate.now();
        Period periodo = Period.between(fechaDeNacimiento, hoy);
        return periodo.getYears();
    }

}
