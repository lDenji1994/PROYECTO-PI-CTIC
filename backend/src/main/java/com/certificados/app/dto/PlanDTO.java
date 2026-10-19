package com.certificados.app.dto;

/**
 * Un plan de estudios de un programa.
 * estado: VIGENTE, ANTIGUO o DESACTUALIZADO.
 * asignaturas: cuantas asignaturas tiene registradas.
 */
public record PlanDTO(
        Integer id,
        Integer idPrograma,
        String nombre,
        String estado,
        long asignaturas
) {
}
