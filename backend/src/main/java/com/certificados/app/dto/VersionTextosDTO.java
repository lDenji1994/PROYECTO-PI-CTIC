package com.certificados.app.dto;

/** Textos largos de una version (SIN el archivo binario), para el certificado completo. */
public record VersionTextosDTO(
        Integer id,
        String proposito,
        String justificacion,
        String requisitos,
        String modalidades
) {
}
