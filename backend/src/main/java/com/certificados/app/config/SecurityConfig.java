package com.certificados.app.config;

import com.certificados.app.repository.UsuarioRepository;
import com.certificados.app.security.SesionVigenteFilter;
import com.certificados.app.security.UsuarioSesion;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.SecurityContextRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * SEGURIDAD: QUIEN PUEDE ENTRAR Y A QUE.
 *
 * - El login es por sesion: el panel envia usuario y contrasena a
 *   POST /api/auth/login (AuthController) y el servidor deja una cookie de
 *   sesion. No hay usuarios ni contrasenas escritos en el codigo: salen de
 *   la tabla de usuarios, con la contrasena guardada con hash BCrypt.
 *
 * - Roles (tabla RolesS):
 *     ADMINISTRADOR : todo, incluido crear usuarios y registrar programas.
 *     AUXILIAR      : cargar documentos, registrar asignaturas y gestionar
 *                     solicitudes y certificados. No puede crear usuarios,
 *                     registrar programas, ver la bitacora completa ni
 *                     cambiar las plantillas del certificado.
 *
 * SE PUEDE MODIFICAR: las reglas de "authorizeHttpRequests" para dar o
 * quitar permisos a un rol. El ORDEN importa: gana la primera regla que
 * coincide, por eso las mas especificas van arriba.
 * NO MODIFICAR: el PasswordEncoder (cambiarlo invalida las contrasenas
 * ya guardadas) ni dejar "anyRequest().permitAll()".
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN = UsuarioSesion.ROL_ADMINISTRADOR;

    /** Rutas de configuracion de las plantillas de certificado. */
    private static final String[] PLANTILLAS = {
        "/api/tipos-certificados/**", "/api/plantillas-certificados/**", "/api/versiones-plantilla/**",
        "/api/secciones-plantilla/**", "/api/campos-plantilla/**", "/api/elementos-plantilla/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           SecurityContextRepository repositorioContexto,
                                           UsuarioRepository usuarioRepository) throws Exception {
        http
            // La API se usa solo desde el propio panel (mismo origen) y la
            // cookie de sesion es SameSite=Strict (application.properties),
            // que es la proteccion contra CSRF de esta aplicacion.
            .csrf(csrf -> csrf.disable())
            .cors(cors -> {})
            .securityContext(contexto -> contexto.securityContextRepository(repositorioContexto))
            // Cierra la sesion de un usuario desactivado o con contrasena cambiada
            .addFilterAfter(new SesionVigenteFilter(usuarioRepository), SecurityContextHolderFilter.class)
            .authorizeHttpRequests(auth -> auth
                // Paginas de error internas de Spring
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                // Publico: pantalla de login y sus recursos
                .requestMatchers("/login.html", "/login.js", "/styles.css", "/favicon.ico").permitAll()
                .requestMatchers("/api/auth/login", "/api/auth/logout").permitAll()
                .requestMatchers("/actuator/health").permitAll()

                // Solo ADMINISTRADOR
                .requestMatchers(HttpMethod.GET, "/api/usuarios").authenticated()
                .requestMatchers("/api/usuarios/**").hasRole(ADMIN)
                .requestMatchers(HttpMethod.POST, "/api/programas/**").hasRole(ADMIN)
                .requestMatchers(HttpMethod.DELETE, "/api/programas/**").hasRole(ADMIN)
                .requestMatchers("/api/logs/**").hasRole(ADMIN)
                // Plantillas del certificado: todos las leen, solo el administrador las cambia
                .requestMatchers(HttpMethod.GET, PLANTILLAS).authenticated()
                .requestMatchers(PLANTILLAS).hasRole(ADMIN)
                // Generar un certificado eligiendo la plantilla a mano (salta el
                // flujo normal). El panel usa /api/solicitudes-certificados/{id}/procesar.
                .requestMatchers(HttpMethod.POST, "/api/certificados-generados/**").hasRole(ADMIN)
                .requestMatchers("/actuator/**").hasRole(ADMIN)

                // ELIMINAR (EliminacionController): documentos cargados, asignaturas y
                // solicitudes sin certificado los puede eliminar cualquier usuario con
                // sesion (queda en la bitacora quien fue). Programas y usuarios, solo
                // el administrador (reglas de arriba). SE PUEDE MODIFICAR: para dejar
                // TODA eliminacion solo al administrador, quita las "//" de la linea
                // siguiente.
                // .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole(ADMIN)

                // Todo lo demas (panel y API): cualquier usuario con sesion
                .anyRequest().authenticated()
            )
            .exceptionHandling(errores -> errores
                // Sin sesion: la API responde 401 en JSON; las paginas van al login
                .authenticationEntryPoint((request, response, ex) -> {
                    if (esPeticionApi(request)) {
                        responderJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                                "No autenticado", "Tu sesión no está activa. Inicia sesión de nuevo.");
                    } else {
                        response.sendRedirect(request.getContextPath() + "/login.html");
                    }
                })
                // Con sesion pero sin permiso
                .accessDeniedHandler((request, response, ex) ->
                        responderJson(response, HttpServletResponse.SC_FORBIDDEN,
                                "Acceso denegado", "Tu usuario no tiene permiso para realizar esta acción."))
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .logout(logout -> logout.disable());

        return http.build();
    }

    /** Donde se guarda la sesion del usuario autenticado (sesion HTTP del servidor). */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /** Las contrasenas se guardan con hash BCrypt: no se pueden leer ni revertir. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Lo usa AuthController para comprobar usuario + contrasena. */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }

    private static boolean esPeticionApi(HttpServletRequest request) {
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        return ruta.startsWith("/api/");
    }

    /** Respuesta de error con el mismo formato que GlobalExceptionHandler ("messages"). */
    private static void responderJson(HttpServletResponse response, int estado,
                                      String error, String mensaje) throws IOException {
        response.setStatus(estado);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"status\":" + estado + ",\"error\":\"" + error
                + "\",\"messages\":[\"" + mensaje + "\"]}");
    }
}
