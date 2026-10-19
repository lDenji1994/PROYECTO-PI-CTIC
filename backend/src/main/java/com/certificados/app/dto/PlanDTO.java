package com.certificados.app.dto;

/**
 * Un plan de estudios de un programa.
 * estado: VIGENTE, ANTIGUO o DESACTUALIZADO.
 * programa: "BCSI - Ingeniería de Sistemas" (para mostrarlo en la lista).
 * asignaturas: cuantas asignaturas tiene registradas.
 */
public record PlanDTO(
        Integer id,
        Integer idPrograma,
        String programa,
        String nombre,
        String estado,
        long asignaturas
) {
}
