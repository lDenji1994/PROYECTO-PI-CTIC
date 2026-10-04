package com.certificados.app.dto;

/**
 * Lo que el panel necesita saber del usuario que inicio sesion.
 * NUNCA incluye la contrasena ni su hash.
 */
public record UsuarioSesionDTO(Integer id, String usuario, String nombreCompleto, String rol, boolean administrador) {
}
