package com.certificados.app.dto;

/** Cuerpo de POST /api/auth/login. */
public record LoginRequestDTO(String usuario, String contrasena) {
}
