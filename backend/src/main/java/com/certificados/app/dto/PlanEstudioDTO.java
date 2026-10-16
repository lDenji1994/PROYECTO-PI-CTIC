package com.certificados.app.dto;

/**
 * Una asignatura del plan de estudios de un programa, con su semestre.
 * codigoMateria / codigoCurso: el codigo separado (igual que en AsignaturaDTO).
 */
public record PlanEstudioDTO(
        Integer idPrograma,
        Integer idAsignatura,
        String codigo,
        String codigoMateria,
        String codigoCurso,
        String nombre,
        Integer semestre
) {
}
