package ar.utn.ba.ddsi.services;

import ar.utn.ba.ddsi.exceptions.NotFoundException;
import ar.utn.ba.ddsi.models.dtos.SingupDTO;
import ar.utn.ba.ddsi.models.dtos.UserRolesPermissionsDTO;
import ar.utn.ba.ddsi.models.entities.Usuario;
import ar.utn.ba.ddsi.models.enums.Permiso;
import ar.utn.ba.ddsi.models.enums.Rol;
import ar.utn.ba.ddsi.models.repositories.IUsuariosRepository;
import ar.utn.ba.ddsi.utils.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class SingupService {

  private final IUsuariosRepository usuariosRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  public SingupService(IUsuariosRepository usuariosRepository) {
    this.usuariosRepository = usuariosRepository;
    this.passwordEncoder = new BCryptPasswordEncoder();
  }

  public Usuario registrarUsuario(SingupDTO dto) {
    // email como username
    String mail = dto.getEmail();

    if (usuariosRepository.findBymail(mail).isPresent()) {
      throw new IllegalArgumentException("Ya existe un usuario con ese email");
    }

    Usuario usuario = new Usuario();
    usuario.setNombre(dto.getNombre());
    usuario.setApellido(dto.getApellido());
    usuario.setNombreDeUsuario(dto.getUsername());
    usuario.setMail(mail);
    usuario.setContrasenia(passwordEncoder.encode(dto.getContrasenia()));
    usuario.setRol(Rol.CONTRIBUYENTE);

    // permisos por defecto para contribuyentes
    usuario.getPermisos().add(Permiso.CREAR_HECHO);
    usuario.getPermisos().add(Permiso.EDITAR_HECHO_PROPIO);
    usuario.getPermisos().add(Permiso.VER_PANEL_DE_CONTROL);

    return usuariosRepository.save(usuario);
  }


}
