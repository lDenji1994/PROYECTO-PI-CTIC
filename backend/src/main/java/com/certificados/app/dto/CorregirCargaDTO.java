package com.certificados.app.dto;

/**
 * Datos de identificacion de un archivo ya cargado que se pueden corregir
 * desde el panel ("Corregir" en Carga de informacion).
 *
 * periodo e idPrograma son opcionales (null = sin periodo / sin programa).
 */
public record CorregirCargaDTO(
        Integer idAsignatura,
        Integer idTipoDocumentoAcademico,
        String codigoFormato,
        String versionFormato,
        String periodo,
        Integer idPrograma
) {
}
