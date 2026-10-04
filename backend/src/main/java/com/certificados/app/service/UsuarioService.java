package com.certificados.app.service;

import com.certificados.app.dto.UsuarioCrearDTO;
import com.certificados.app.dto.UsuarioResumenDTO;
import com.certificados.app.exception.BusinessException;
import com.certificados.app.exception.ResourceNotFoundException;
import com.certificados.app.model.Rol;
import com.certificados.app.model.Usuario;
import com.certificados.app.repository.RolRepository;
import com.certificados.app.repository.UsuarioRepository;
import com.certificados.app.security.UsuarioSesion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * GESTION DE USUARIOS (la hace el ADMINISTRADOR desde el panel).
 *
 * El administrador crea las cuentas de las auxiliares y les asigna su
 * contrasena inicial; cada auxiliar puede cambiarla despues.
 *
 * Reglas:
 *  - La contrasena se guarda SIEMPRE con hash BCrypt (PasswordEncoder).
 *  - Maximo de auxiliares activas: app.seguridad.max-auxiliares (4 por defecto).
 *  - No se puede desactivar al ultimo administrador activo.
 *
 * SE PUEDE MODIFICAR: el maximo de auxiliares (application.properties) y
 * las reglas de la contrasena (PATRON_CONTRASENA / validarContrasena).
 */
@Service
@Transactional
public class UsuarioService {

    private static final Pattern PATRON_USUARIO = Pattern.compile("^[a-z0-9][a-z0-9._-]{2,29}$");
    private static final Pattern PATRON_CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActividadService actividadService;
    private final int maxAuxiliares;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          PasswordEncoder passwordEncoder,
                          ActividadService actividadService,
                          @Value("${app.seguridad.max-auxiliares:4}") int maxAuxiliares) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.actividadService = actividadService;
        this.maxAuxiliares = maxAuxiliares;
    }

    public int maxAuxiliares() {
        return maxAuxiliares;
    }

    /** Lista de usuarios. soloActivos=true para las listas desplegables del panel. */
    @Transactional(readOnly = true)
    public List<UsuarioResumenDTO> listar(boolean soloActivos) {
        Map<Integer, String> roles = nombresDeRoles();
        return usuarioRepository.findAll().stream()
                .filter(u -> !soloActivos || estaActivo(u))
                .map(u -> aDTO(u, roles))
                .sorted(Comparator.comparing(UsuarioResumenDTO::nombreCompleto, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public UsuarioResumenDTO crear(UsuarioCrearDTO datos) {
        String nombre = datos.nombreCompleto() == null ? "" : datos.nombreCompleto().trim().replaceAll("\\s+", " ");
        String usuario = datos.usuario() == null ? "" : datos.usuario().trim().toLowerCase(Locale.ROOT);
        String correo = datos.correo() == null || datos.correo().isBlank() ? null : datos.correo().trim().toLowerCase(Locale.ROOT);
        String nombreRol = datos.rol() == null ? "" : datos.rol().trim().toUpperCase(Locale.ROOT);

        if (nombre.length() < 3 || nombre.length() > 150) {
            throw new BusinessException("El nombre completo debe tener entre 3 y 150 caracteres");
        }
        if (!PATRON_USUARIO.matcher(usuario).matches()) {
            throw new BusinessException("El usuario debe tener de 3 a 30 caracteres: letras minúsculas, números, punto, guion o guion bajo");
        }
        if (correo != null && (correo.length() > 150 || !PATRON_CORREO.matcher(correo).matches())) {
            throw new BusinessException("El correo no tiene un formato válido");
        }
        validarContrasena(datos.contrasena(), usuario);

        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new BusinessException("Rol no válido: use ADMINISTRADOR o AUXILIAR"));

        if (usuarioRepository.existsByUsuario(usuario)) {
            throw new BusinessException("Ya existe un usuario con ese nombre de usuario");
        }
        if (correo != null && usuarioRepository.existsByCorreo(correo)) {
            throw new BusinessException("Ya existe un usuario con ese correo");
        }
        if (UsuarioSesion.ROL_AUXILIAR.equals(nombreRol)) {
            validarCupoAuxiliares(rol.getId());
        }

        Usuario nuevo = new Usuario();
        nuevo.setNombreCompleto(nombre);
        nuevo.setUsuario(usuario);
        nuevo.setCorreo(correo);
        nuevo.setContrasena(passwordEncoder.encode(datos.contrasena()));
        nuevo.setActivo(true);
        nuevo.setIdRol(rol.getId());
        Usuario guardado = usuarioRepository.save(nuevo);

        actividadService.registrar(ActividadService.TABLA_USUARIOS, ActividadService.CREAR_USUARIO,
                usuario + " (" + nombreRol + ")");
        return aDTO(guardado, nombresDeRoles());
    }

    /** Activa o desactiva un usuario. Un usuario desactivado no puede iniciar sesion. */
    public UsuarioResumenDTO cambiarEstado(Integer id, boolean activo, Integer idQuienLoHace) {
        Usuario usuario = obtener(id);
        Map<Integer, String> roles = nombresDeRoles();
        String rol = roles.getOrDefault(usuario.getIdRol(), "");

        if (!activo) {
            if (id.equals(idQuienLoHace)) {
                throw new BusinessException("No puedes desactivar tu propio usuario");
            }
            if (UsuarioSesion.ROL_ADMINISTRADOR.equals(rol) && contarActivos(usuario.getIdRol()) <= 1) {
                throw new BusinessException("No se puede desactivar al único administrador activo");
            }
        } else if (!estaActivo(usuario) && UsuarioSesion.ROL_AUXILIAR.equals(rol)) {
            validarCupoAuxiliares(usuario.getIdRol());
        }

        usuario.setActivo(activo);
        Usuario guardado = usuarioRepository.save(usuario);

        actividadService.registrar(ActividadService.TABLA_USUARIOS,
                activo ? ActividadService.ACTIVAR_USUARIO : ActividadService.DESACTIVAR_USUARIO,
                usuario.getUsuario());
        return aDTO(guardado, roles);
    }

    /** El administrador asigna una contrasena nueva a otro usuario. */
    public void restablecerContrasena(Integer id, String nueva) {
        Usuario usuario = obtener(id);
        validarContrasena(nueva, usuario.getUsuario());
        usuario.setContrasena(passwordEncoder.encode(nueva));
        usuarioRepository.save(usuario);

        actividadService.registrar(ActividadService.TABLA_USUARIOS,
                ActividadService.CAMBIAR_CONTRASENA, usuario.getUsuario() + " (por el administrador)");
    }

    /** El propio usuario cambia su contrasena; debe escribir la actual. */
    public void cambiarContrasenaPropia(Integer id, String actual, String nueva) {
        Usuario usuario = obtener(id);
        if (actual == null || !passwordEncoder.matches(actual, usuario.getContrasena())) {
            throw new BusinessException("La contraseña actual no es correcta");
        }
        validarContrasena(nueva, usuario.getUsuario());
        if (passwordEncoder.matches(nueva, usuario.getContrasena())) {
            throw new BusinessException("La contraseña nueva debe ser diferente de la actual");
        }
        usuario.setContrasena(passwordEncoder.encode(nueva));
        usuarioRepository.save(usuario);

        actividadService.registrar(ActividadService.TABLA_USUARIOS,
                ActividadService.CAMBIAR_CONTRASENA, usuario.getUsuario());
    }

    /* ----------------------------- utilidades ----------------------------- */

    /** Minimo 8 caracteres, con letras y numeros, distinta del nombre de usuario. */
    public static void validarContrasena(String contrasena, String usuario) {
        // BCrypt solo usa los primeros 72 BYTES (una letra con tilde ocupa 2)
        if (contrasena == null || contrasena.length() < 8
                || contrasena.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException("La contraseña debe tener entre 8 y 72 caracteres");
        }
        boolean letra = contrasena.chars().anyMatch(Character::isLetter);
        boolean digito = contrasena.chars().anyMatch(Character::isDigit);
        if (!letra || !digito) {
            throw new BusinessException("La contraseña debe incluir letras y números");
        }
        if (usuario != null && contrasena.equalsIgnoreCase(usuario)) {
            throw new BusinessException("La contraseña no puede ser igual al usuario");
        }
    }

    private void validarCupoAuxiliares(Integer idRolAuxiliar) {
        if (maxAuxiliares > 0 && contarActivos(idRolAuxiliar) >= maxAuxiliares) {
            throw new BusinessException("Ya hay " + maxAuxiliares
                    + " auxiliares activas, que es el máximo permitido. Desactiva una para crear otra.");
        }
    }

    private long contarActivos(Integer idRol) {
        return usuarioRepository.findAll().stream()
                .filter(u -> idRol.equals(u.getIdRol()) && estaActivo(u))
                .count();
    }

    private Usuario obtener(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + id));
    }

    private Map<Integer, String> nombresDeRoles() {
        return rolRepository.findAll().stream()
                .collect(Collectors.toMap(Rol::getId, r -> r.getNombre().trim().toUpperCase(Locale.ROOT), (a, b) -> a));
    }

    private static boolean estaActivo(Usuario u) {
        return !Boolean.FALSE.equals(u.getActivo());
    }

    private static UsuarioResumenDTO aDTO(Usuario u, Map<Integer, String> roles) {
        return new UsuarioResumenDTO(u.getId(), u.getNombreCompleto(), u.getUsuario(),
                estaActivo(u), roles.getOrDefault(u.getIdRol(), ""));
    }
}
