USE CertificadosCursosAcademicosUPB;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE certificados_generados;
TRUNCATE TABLE solicitud_asignaturas;
TRUNCATE TABLE solicitudes;

TRUNCATE TABLE contenidos;

TRUNCATE TABLE versiones_documento;
TRUNCATE TABLE documentos;

TRUNCATE TABLE planes_estudio;
TRUNCATE TABLE asignaturas;
TRUNCATE TABLE programas;

TRUNCATE TABLE bitacora;

SET FOREIGN_KEY_CHECKS = 1;

SELECT
    (SELECT COUNT(*) FROM asignaturas)               AS asignaturas,
    (SELECT COUNT(*) FROM programas)                 AS programas,
    (SELECT COUNT(*) FROM versiones_documento)       AS documentos_cargados,
    (SELECT COUNT(*) FROM solicitudes)   AS solicitudes,
    (SELECT COUNT(*) FROM bitacora)                       AS actividad,
    (SELECT COUNT(*) FROM usuarios)                  AS usuarios_conservados,
    (SELECT COUNT(*) FROM elementos_plantilla)        AS elementos_plantilla;
