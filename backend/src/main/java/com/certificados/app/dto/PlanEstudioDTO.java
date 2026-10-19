package com.certificados.app.dto;

/**
 * Una asignatura de un plan de estudios, con su semestre.
 * codigoMateria / codigoCurso: el codigo separado (igual que en AsignaturaDTO).
 */
public record PlanEstudioDTO(
        Integer idPlan,
        Integer idAsignatura,
        String codigo,
        String codigoMateria,
        String codigoCurso,
        String nombre,
        Integer semestre
) {
}
