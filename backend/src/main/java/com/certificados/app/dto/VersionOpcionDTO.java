package com.certificados.app.dto;

import java.time.LocalDateTime;

/**
 * Una version de documento que se puede elegir para el certificado de una
 * asignatura (lista desplegable de la vista previa de la solicitud).
 *
 * datosCompletos = tiene descripcion del curso y creditos registrados.
 */
public record VersionOpcionDTO(
        Integer idVersionDocumento,
        String periodo,
        String tipoDocumento,
        String formato,
        boolean formatoVigente,
        LocalDateTime fechaCarga,
        boolean datosCompletos
) {
}
