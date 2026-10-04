package com.certificados.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Datos livianos de una version cargada (SIN el archivo binario).
 * Se usa para listar sin traer los archivos completos desde la BD.
 *
 * creditos y descripcion permiten al panel saber si la version ya tiene
 * los datos del curso registrados a mano ("Datos completos") o si es una
 * carga antigua a la que hay que completarle la informacion.
 */
public record VersionResumenDTO(
        Integer id,
        Integer idDocumentoAcademico,
        String periodo,
        String nombreArchivo,
        LocalDateTime fechaCarga,
        BigDecimal creditos,
        String descripcion
) {
}
