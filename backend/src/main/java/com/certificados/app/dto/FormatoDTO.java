package com.certificados.app.dto;

/**
 * Un formato institucional de documento academico (catalogo configurable).
 *
 * @param codigo  codigo del formato, p. ej. DA-FO-085N
 * @param version version del formato, p. ej. 03
 * @param nombre  nombre visible, p. ej. "Carta descriptiva del curso"
 * @param vigente true si es el formato que la universidad exige hoy
 */
public record FormatoDTO(String codigo, String version, String nombre, boolean vigente) {
}
