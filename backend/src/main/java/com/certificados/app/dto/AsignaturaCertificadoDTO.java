package com.certificados.app.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lo que el certificado de contenidos dice de UNA asignatura.
 *
 * completo  = true si tiene todo lo necesario para aparecer en el certificado.
 * faltantes = que le falta (documento, descripcion, contenido, creditos...).
 *             El panel lo muestra para que la auxiliar lo complete.
 * versionElegidaManual  = true si la auxiliar escogio la version a mano;
 *                         false si la eligio el sistema.
 * versionesDisponibles  = todas las versiones que se pueden escoger.
 * resumen   = parrafo breve para el certificado RESUMIDO (si no hay, la descripcion).
 * semestre  = semestre de la asignatura en el programa de la solicitud
 *             (solo alcance SEMESTRES o PROGRAMA; si no, null).
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
        List<String> faltantes,
        boolean versionElegidaManual,
        List<VersionOpcionDTO> versionesDisponibles,
        String resumen,
        Integer semestre
) {
}
