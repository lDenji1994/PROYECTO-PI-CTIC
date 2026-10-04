package com.certificados.app.dto;

/**
 * Datos PUBLICOS de un usuario (listas del panel y gestion de usuarios).
 * NO MODIFICAR para agregar la contrasena: nunca debe salir del backend.
 */
public record UsuarioResumenDTO(Integer id, String nombreCompleto, String usuario, Boolean activo, String rol) {
}
