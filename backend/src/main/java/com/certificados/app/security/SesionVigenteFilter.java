package com.certificados.app.security;

import com.certificados.app.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * COMPRUEBA EN CADA PETICION QUE LA SESION SIGA SIENDO VALIDA.
 *
 * Los datos del usuario se guardan en la sesion al iniciar sesion. Sin
 * este filtro, una auxiliar a la que el administrador desactivo (o le
 * cambio la contrasena) podria seguir trabajando con la sesion que ya
 * tenia abierta. Aqui se vuelve a mirar la base de datos y, si el
 * usuario ya no existe, esta inactivo o su contrasena cambio, la sesion
 * se cierra y la siguiente respuesta es 401 (el panel envia al login).
 *
 * NO MODIFICAR: no es un @Component a proposito. Lo registra
 * SecurityConfig dentro de la cadena de seguridad; si fuera un
 * componente, Spring Boot lo registraria una segunda vez.
 */
public class SesionVigenteFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepository;

    public SesionVigenteFilter(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion != null && autenticacion.getPrincipal() instanceof UsuarioSesion sesion) {
            boolean vigente = usuarioRepository.findById(sesion.getId())
                    .map(u -> !Boolean.FALSE.equals(u.getActivo())
                            && Objects.equals(u.getContrasena(), sesion.getPassword()))
                    .orElse(false);
            if (!vigente) {
                HttpSession http = request.getSession(false);
                if (http != null) {
                    http.invalidate();
                }
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
