package com.certificados.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Datos del curso de una version, SIN el archivo binario.
 * Lo usa ContenidoCertificadoService para armar el certificado sin cargar
 * en memoria los PDF / Word / Excel de cada asignatura.
 */
public record VersionDatosDTO(
        Integer id,
        Integer idDocumentoAcademico,
        String periodo,
        LocalDateTime fechaCarga,
        BigDecimal creditos,
        BigDecimal horasTeoricas,
        BigDecimal horasPracticas,
        BigDecimal horasLaboratorio,
        BigDecimal horasIndependientes,
        String descripcion
) {
}
