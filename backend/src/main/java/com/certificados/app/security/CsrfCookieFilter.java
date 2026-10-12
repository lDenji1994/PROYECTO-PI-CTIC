package com.certificados.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Asegura que el navegador reciba la cookie XSRF-TOKEN desde la primera
 * pagina (incluido el login), para que el panel pueda enviarla de vuelta
 * en la cabecera X-XSRF-TOKEN en cada POST / PUT / PATCH / DELETE.
 *
 * Spring Security genera el token "perezosamente" (solo si alguien lo
 * pide); este filtro lo pide en cada peticion para que la cookie exista.
 *
 * NO MODIFICAR: no es un @Component a proposito (lo registra SecurityConfig).
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Object token = request.getAttribute(CsrfToken.class.getName());
        if (token instanceof CsrfToken csrfToken) {
            csrfToken.getToken();
        }
        chain.doFilter(request, response);
    }
}
