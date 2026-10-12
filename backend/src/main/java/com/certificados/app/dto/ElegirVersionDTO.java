package com.certificados.app.dto;

/**
 * Cuerpo de PUT /api/solicitudes-certificados/{id}/asignaturas/{idAsignatura}/version
 *   { "idVersionDocumento": 15 }    -> usar esa version
 *   { "idVersionDocumento": null }  -> volver a la eleccion automatica
 */
public record ElegirVersionDTO(Integer idVersionDocumento) {
}
