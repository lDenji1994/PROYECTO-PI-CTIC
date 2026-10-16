package com.certificados.app.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Todo lo que el certificado COMPLETO muestra de una asignatura, ademas de
 * lo que ya trae AsignaturaCertificadoDTO (creditos, horas, descripcion).
 *
 * Cada lista puede venir vacia: el PDF solo dibuja las secciones que tienen
 * informacion registrada.
 */
public record DetalleCompletoDTO(
        String proposito,
        String justificacion,
        String requisitos,
        String modalidades,
        List<String> objetivos,
        List<CompetenciaDetalle> competencias,
        List<TemaDetalle> contenidos,
        List<String> metodologias,
        List<EvaluacionDetalle> evaluaciones
) {
    /** Competencia con sus criterios. */
    public record CompetenciaDetalle(String descripcion, List<String> criterios) {
    }

    /** Un tema del contenido (semana, unidad y tema son opcionales). */
    public record TemaDetalle(Integer semana, String unidad, String tema, String descripcion) {
    }

    /** Una evaluacion (tipo y porcentaje son opcionales). */
    public record EvaluacionDetalle(String tipo, String descripcion, BigDecimal porcentaje) {
    }
}
