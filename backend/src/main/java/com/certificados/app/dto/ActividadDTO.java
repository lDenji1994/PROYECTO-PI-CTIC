package com.certificados.app.dto;

import java.time.LocalDateTime;

/**
 * Una fila de la "Actividad reciente" del Dashboard, ya lista para mostrar.
 * Se construye a partir de un registro de la tabla de Logs.
 *
 * @param proceso  codigo tecnico del proceso (p. ej. CREAR_ASIGNATURA)
 * @param titulo   texto legible del proceso ("Asignatura registrada")
 * @param detalle  sobre que registro se hizo (p. ej. "FION 0001 - Ondas")
 * @param modulo   documentos | cursos | certificaciones | otros
 * @param nivel    success | warning | danger | info  (color del punto en el panel)
 * @param estado   FINALIZADO | EN_CURSO
 */
public record ActividadDTO(
        Integer id,
        String proceso,
        String titulo,
        String detalle,
        String tabla,
        String modulo,
        String nivel,
        String estado,
        String usuario,
        String ip,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin
) {
}
