package com.certificados.app.dto;

/**
 * Cambio de contrasena.
 *  - El propio usuario (POST /api/auth/cambiar-contrasena): actual + nueva.
 *  - El administrador sobre otro usuario (PATCH /api/usuarios/{id}/contrasena): solo nueva.
 */
public record CambioContrasenaDTO(String actual, String nueva) {
}
