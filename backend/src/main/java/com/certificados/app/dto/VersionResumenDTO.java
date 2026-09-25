package com.certificados.app.dto;

import java.time.LocalDateTime;

/**
 * Datos livianos de una version cargada (SIN el archivo binario).
 * Se usa para listar sin traer los PDF completos desde la BD.
 */
public record VersionResumenDTO(
        Integer id,
        Integer idDocumentoAcademico,
        String periodo,
        String nombreArchivo,
        LocalDateTime fechaCarga
) {
}
