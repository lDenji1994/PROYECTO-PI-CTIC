package com.certificados.app.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lo que el certificado de contenidos dice de UNA asignatura.
 *
 * completo  = true si tiene todo lo necesario para aparecer en el certificado.
 * faltantes = que le falta (documento, descripcion, contenido, creditos...).
 *             El panel lo muestra para que la auxiliar lo complete.
 */
public record AsignaturaCertificadoDTO(
        Integer idAsignatura,
        String codigoMateria,
        String codigoCurso,
        String codigo,
        String nombre,
        Integer idVersionDocumento,
        String tipoDocumento,
        String formato,
        boolean formatoVigente,
        String periodo,
        BigDecimal creditos,
        BigDecimal horasTeoricas,
        BigDecimal horasPracticas,
        BigDecimal horasLaboratorio,
        BigDecimal horasIndependientes,
        String descripcion,
        List<String> contenidos,
        boolean completo,
        List<String> faltantes
) {
}
