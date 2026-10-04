package com.certificados.app.security;

import com.certificados.app.exception.BusinessException;
import com.certificados.app.model.Rol;
import com.certificados.app.model.Usuario;
import com.certificados.app.repository.RolRepository;
import com.certificados.app.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import com.certificados.app.service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * CREA (O HABILITA) AL ADMINISTRADOR INICIAL AL ARRANCAR EL SERVIDOR.
 *
 * Sin esto nadie podria entrar la primera vez, porque las contrasenas no
 * se escriben en el codigo ni en los scripts SQL.
 *
 * Como funciona: en TU archivo application-local.properties (que git
 * ignora) defines:
 *
 *     app.seguridad.admin-inicial.usuario=admin
 *     app.seguridad.admin-inicial.contrasena=LaClaveQueTuElijas1
 *
 * Al arrancar:
 *   - si ese usuario no existe, se crea con rol ADMINISTRADOR;
 *   - si existe pero aun no tiene una contrasena valida (por ejemplo el
 *     "admin" del script 02_datos_iniciales.sql), se le asigna esta;
 *   - si ya tiene contrasena, NO se toca (para cambiarla, usar el panel).
 *
 * Con "app.seguridad.admin-inicial.forzar=true" se vuelve a asignar la
 * contrasena del archivo (sirve si el administrador olvido la suya).
 *
 * Despues el administrador crea las cuentas de las auxiliares desde el panel.
 */
@Component
public class AdminInicialRunner implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AdminInicialRunner.class);

    /** Asi empieza el valor de relleno de application-local.properties.example. */
    private static final String PREFIJO_EJEMPLO = "CAMBIA_";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final String usuario;
    private final String contrasena;
    private final String nombre;
    private final boolean forzar;

    public AdminInicialRunner(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seguridad.admin-inicial.usuario:admin}") String usuario,
            @Value("${app.seguridad.admin-inicial.contrasena:}") String contrasena,
            @Value("${app.seguridad.admin-inicial.nombre:Administrador CTIC}") String nombre,
            @Value("${app.seguridad.admin-inicial.forzar:false}") boolean forzar) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuario = usuario == null ? "" : usuario.trim().toLowerCase(Locale.ROOT);
        this.contrasena = contrasena == null ? "" : contrasena;
        this.nombre = nombre;
        this.forzar = forzar;
    }

    // Sin @Transactional a proposito: cada consulta usa su propia transaccion,
    // asi un fallo (p. ej. tablas inexistentes en las pruebas) se puede
    // capturar aqui sin tumbar el arranque del servidor.
    @Override
    public void run(ApplicationArguments args) {
        try {
            prepararAdministrador();
        } catch (RuntimeException e) {
            // No se detiene el arranque (p. ej. en las pruebas, sin tablas)
            LOG.warn("No se pudo preparar el administrador inicial: {}", e.getMessage());
        }
    }

    private void prepararAdministrador() {
        Usuario existente = usuarioRepository.findByUsuario(usuario).orElse(null);
        boolean tieneContrasena = existente != null && esHashBCrypt(existente.getContrasena());

        if (contrasena.isBlank()) {
            if (!tieneContrasena) {
                LOG.warn("LOGIN: el usuario '{}' no tiene contrasena. Define "
                        + "app.seguridad.admin-inicial.contrasena en application-local.properties "
                        + "y reinicia el servidor para poder iniciar sesion.", usuario);
            }
            return;
        }

        if (tieneContrasena && !forzar) {
            return;
        }

        // Misma regla que para cualquier usuario; ademas no se acepta el
        // texto de ejemplo del archivo .example (es publico en el repositorio).
        if (contrasena.toUpperCase(Locale.ROOT).startsWith(PREFIJO_EJEMPLO)) {
            LOG.warn("LOGIN: app.seguridad.admin-inicial.contrasena aun tiene el texto de ejemplo. "
                    + "Escribe tu propia contrasena en application-local.properties y reinicia.");
            return;
        }
        try {
            UsuarioService.validarContrasena(contrasena, usuario);
        } catch (BusinessException e) {
            LOG.warn("LOGIN: la contrasena del administrador inicial no es valida: {}", e.getMessage());
            return;
        }

        if (existente == null) {
            Rol rol = rolRepository.findByNombre(UsuarioSesion.ROL_ADMINISTRADOR).orElse(null);
            if (rol == null) {
                LOG.warn("LOGIN: no existe el rol ADMINISTRADOR en la base de datos; "
                        + "ejecuta el script de esquema antes de arrancar.");
                return;
            }
            existente = new Usuario();
            existente.setUsuario(usuario);
            existente.setNombreCompleto(nombre);
            existente.setIdRol(rol.getId());
        }
        existente.setActivo(true);
        existente.setContrasena(passwordEncoder.encode(contrasena));
        usuarioRepository.save(existente);

        // Nunca se escribe la contrasena en la consola
        LOG.info("LOGIN: administrador inicial '{}' listo para iniciar sesion.", usuario);
    }

    private static boolean esHashBCrypt(String valor) {
        return valor != null && valor.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }
}
