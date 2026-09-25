package com.certificados.app.dto;

/**
 * Datos PUBLICOS de un usuario (para listas desplegables del panel).
 * NO MODIFICAR para agregar la contrasena: nunca debe salir del backend.
 */
public record UsuarioResumenDTO(Integer id, String nombreCompleto, String usuario, Boolean activo) {
}
