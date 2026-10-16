package com.certificados.app.dto;

import java.util.List;

/**
 * Todo lo que el certificado COMPLETO muestra de una asignatura, ademas de
 * lo que ya trae AsignaturaCertificadoDTO (creditos, horas, descripcion).
 *
 * Los textos pueden venir vacios: el PDF solo dibuja las secciones que
 * tienen informacion registrada.
 */
public record DetalleCompletoDTO(
        String proposito,
        String justificacion,
        String requisitos,
        String modalidades,
        List<TemaDetalle> contenidos
) {
    /** Un tema del contenido (semana, unidad y tema son opcionales). */
    public record TemaDetalle(Integer semana, String unidad, String tema, String descripcion) {
    }
}
