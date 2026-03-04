package ar.utn.ba.ddsi.models.entities;

import ar.utn.ba.ddsi.models.enums.Permiso;
import ar.utn.ba.ddsi.models.enums.Rol;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name="usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nombre")
    private String nombre;
    @Column(name = "apellido")
    private String apellido;
    @Column(name = "nombre_usuario", nullable = false, unique = true, length = 50)
    private String nombreDeUsuario;
    @Column(name = "contrasenia", nullable = false)
    private String contrasenia; // cifrada
    @Column(name = "mail")
    private String mail;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "permisos_usuario", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "permiso")
    private List<Permiso> permisos = new ArrayList<>();
    public void agregarPermiso(Permiso p) {
        this.permisos.add(p);
    }

}
