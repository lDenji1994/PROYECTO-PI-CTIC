package com.certificados.app.controller;

import com.certificados.app.dto.CambioContrasenaDTO;
import com.certificados.app.dto.LoginRequestDTO;
import com.certificados.app.dto.UsuarioSesionDTO;
import com.certificados.app.exception.BusinessException;
import com.certificados.app.security.LimitadorIntentos;
import com.certificados.app.security.UsuarioDetallesService;
import com.certificados.app.security.UsuarioSesion;
import com.certificados.app.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * INICIO Y CIERRE DE SESION.
 *
 *   POST /api/auth/login                { "usuario": "...", "contrasena": "..." }
 *   GET  /api/auth/yo                   -> quien tiene la sesion (401 si nadie)
 *   POST /api/auth/logout
 *   POST /api/auth/cambiar-contrasena   { "actual": "...", "nueva": "..." }
 *
 * Seguridad:
 *  - El mensaje de error es el mismo si falla el usuario o la contrasena
 *    (no se revela cual de los dos existe).
 *  - Tras varios intentos fallidos el usuario se bloquea unos minutos
 *    para el equipo desde donde se intento (LimitadorIntentos).
 *  - Al entrar se cambia el identificador de sesion (evita "session fixation").
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository repositorioContexto;
    private final LimitadorIntentos limitador;
    private final UsuarioService usuarioService;
    private final UsuarioDetallesService usuarioDetallesService;

    public AuthController(AuthenticationManager authenticationManager,
                          SecurityContextRepository repositorioContexto,
                          LimitadorIntentos limitador,
                          UsuarioService usuarioService,
                          UsuarioDetallesService usuarioDetallesService) {
        this.usuarioDetallesService = usuarioDetallesService;
        this.authenticationManager = authenticationManager;
        this.repositorioContexto = repositorioContexto;
        this.limitador = limitador;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public UsuarioSesionDTO login(@RequestBody LoginRequestDTO datos,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {

        String usuario = datos.usuario() == null ? "" : datos.usuario().trim();
        String contrasena = datos.contrasena() == null ? "" : datos.contrasena();

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            throw new BusinessException("Escribe tu usuario y tu contraseña");
        }

        String ip = request.getRemoteAddr();
        long minutos = limitador.minutosBloqueado(usuario, ip);
        if (minutos > 0) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Demasiados intentos fallidos. Intenta de nuevo en " + minutos + " min.");
        }

        Authentication autenticacion;
        try {
            autenticacion = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(usuario, contrasena));
        } catch (AuthenticationException e) {
            limitador.registrarFallo(usuario, ip);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
        }
        limitador.registrarExito(usuario, ip);

        // Sesion nueva para el usuario autenticado
        HttpSession anterior = request.getSession(false);
        if (anterior != null) {
            request.changeSessionId();
        }
        guardarEnSesion(autenticacion, request, response);

        return aDTO((UsuarioSesion) autenticacion.getPrincipal());
    }

    @GetMapping("/yo")
    public UsuarioSesionDTO yo() {
        return UsuarioDetallesService.actual()
                .map(AuthController::aDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sin sesión"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cambiar-contrasena")
    public ResponseEntity<Void> cambiarContrasena(@RequestBody CambioContrasenaDTO datos,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) {
        UsuarioSesion sesion = UsuarioDetallesService.actual()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sin sesión"));
        usuarioService.cambiarContrasenaPropia(sesion.getId(), datos.actual(), datos.nueva());

        // La sesion guarda una copia de los datos del usuario: se actualiza
        // para que SesionVigenteFilter no la cierre por "contrasena cambiada".
        UsuarioSesion actualizado = usuarioDetallesService.loadUserByUsername(sesion.getUsername());
        guardarEnSesion(UsernamePasswordAuthenticationToken.authenticated(
                actualizado, null, actualizado.getAuthorities()), request, response);
        return ResponseEntity.noContent().build();
    }

    private void guardarEnSesion(Authentication autenticacion,
                                 HttpServletRequest request, HttpServletResponse response) {
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacion);
        SecurityContextHolder.setContext(contexto);
        repositorioContexto.saveContext(contexto, request, response);
    }

    private static UsuarioSesionDTO aDTO(UsuarioSesion u) {
        return new UsuarioSesionDTO(u.getId(), u.getUsername(), u.getNombreCompleto(), u.getRol(), u.esAdministrador());
    }
}
