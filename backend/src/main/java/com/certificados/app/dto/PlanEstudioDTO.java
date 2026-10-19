package com.certificados.app.dto;

/**
 * Una asignatura del plan de estudios de un programa, con su semestre.
 * codigoMateria / codigoCurso: el codigo separado (igual que en AsignaturaDTO).
 * plan: nombre o periodo del plan (ej. "Plan 2026"); puede venir vacio.
 * estadoPlan: VIGENTE, ANTIGUO o DESACTUALIZADO.
 */
public record PlanEstudioDTO(
        Integer idPrograma,
        Integer idAsignatura,
        String codigo,
        String codigoMateria,
        String codigoCurso,
        String nombre,
        Integer semestre,
        String plan,
        String estadoPlan
) {
}
