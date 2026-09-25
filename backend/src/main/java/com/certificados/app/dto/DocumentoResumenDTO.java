package com.certificados.app.dto;

import java.util.List;

/**
 * Vista completa de un documento academico para el panel
 * ("Documentos recientes", "Contenido de cursos").
 *
 * estadoFormato: VIGENTE | DESACTUALIZADO  (ver FormatoService)
 */
public record DocumentoResumenDTO(
        Integer id,
        Integer idAsignatura,
        String codigoMateria,
        String codigoCurso,
        String codigoAsignatura,
        String nombreAsignatura,
        Integer idTipoDocumento,
        String tipoDocumento,
        String codigoFormato,
        String versionFormato,
        String nombreFormato,
        boolean vigente,
        String estadoFormato,
        int totalVersiones,
        VersionResumenDTO ultimaVersion,
        List<VersionResumenDTO> versiones
) {
}
