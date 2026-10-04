package com.certificados.app.security;

import com.certificados.app.model.Rol;
import com.certificados.app.model.Usuario;
import com.certificados.app.repository.RolRepository;
import com.certificados.app.repository.UsuarioRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

/**
 * Le dice a Spring Security como buscar un usuario para el login:
 * en la tabla de usuarios de la base de datos (ya no hay usuarios
 * escritos en el codigo).
 */
@Service
public class UsuarioDetallesService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public UsuarioDetallesService(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioSesion loadUserByUsername(String username) throws UsernameNotFoundException {
        String nombre = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        // La base de datos compara sin distinguir tildes ("ádmin" = "admin").
        // Aqui se exige que el nombre sea exactamente el guardado.
        Usuario usuario = usuarioRepository.findByUsuario(nombre)
                .filter(u -> u.getUsuario() != null
                        && u.getUsuario().trim().toLowerCase(Locale.ROOT).equals(nombre))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        String rol = rolRepository.findById(usuario.getIdRol())
                .map(Rol::getNombre)
                .map(r -> r.trim().toUpperCase(Locale.ROOT))
                .orElse(UsuarioSesion.ROL_AUXILIAR);

        return new UsuarioSesion(
                usuario.getId(),
                usuario.getUsuario(),
                usuario.getNombreCompleto(),
                rol,
                usuario.getContrasena(),
                !Boolean.FALSE.equals(usuario.getActivo()));
    }

    /** Usuario de la sesion actual, si hay alguien autenticado. */
    public static Optional<UsuarioSesion> actual() {
        var autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion != null && autenticacion.getPrincipal() instanceof UsuarioSesion sesion) {
            return Optional.of(sesion);
        }
        return Optional.empty();
    }
}
