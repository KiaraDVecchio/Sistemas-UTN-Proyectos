package domain;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

public class Contribuyente {
  private String nombre;
  private String apellido;
  private LocalDate fechaDeNacimiento;

  public Contribuyente(String nombre, LocalDate fechaDeNacimiento, String apellido) {
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
