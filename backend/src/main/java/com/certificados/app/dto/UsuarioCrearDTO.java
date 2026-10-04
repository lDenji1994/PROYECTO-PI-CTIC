package com.certificados.app.dto;

/**
 * Datos para que el administrador cree un usuario.
 * rol: ADMINISTRADOR o AUXILIAR (nombre del rol en la tabla RolesS).
 */
public record UsuarioCrearDTO(String nombreCompleto, String usuario, String correo, String contrasena, String rol) {
}
