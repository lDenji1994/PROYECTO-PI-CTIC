-- =====================================================================
--  VACIAR LOS DATOS DE PRUEBA (dejar la base "en blanco" para usarla)
--
--  ¡CUIDADO! Borra para siempre, sin poder deshacer:
--    - asignaturas, programas y plan de estudios
--    - documentos cargados (archivos PDF/Word/Excel) y datos del curso
--    - solicitudes, certificados generados (PDF) y su detalle
--    - la bitacora de actividad (Log)
--  y los contadores de ID vuelven a empezar en 1.
--
--  CONSERVA (lo necesario para que la web funcione):
--    - roles y usuarios (con sus contraseñas)
--    - tipos de documento y tipos de certificado
--    - la plantilla del certificado (versiones, secciones, campos, elementos)
--
--  Como usarlo: en Workbench, File -> Open SQL Script, abrir este archivo
--  y ejecutarlo con el rayo. Hacer un respaldo antes si hay algo que
--  se quiera guardar (Server -> Data Export).
--
--  SE PUEDE MODIFICAR: quitar de la lista una tabla que se quiera conservar
--  (por ejemplo, asignaturas y programas si ya son los reales).
-- =====================================================================
USE CertificadosCursosAcademicosUPB;

-- Se desactiva la revision de llaves foraneas solo mientras se vacia
SET FOREIGN_KEY_CHECKS = 0;

-- Certificados y solicitudes
TRUNCATE TABLE certificados_generados;
TRUNCATE TABLE solicitud_asignaturas;
TRUNCATE TABLE solicitudes;

-- Contenido (tema por tema) de cada documento cargado
TRUNCATE TABLE contenidos;

-- Documentos cargados (con sus archivos)
TRUNCATE TABLE versiones_documento;
TRUNCATE TABLE documentos;

-- Asignaturas, programas y plan de estudios
TRUNCATE TABLE planes_estudio;
TRUNCATE TABLE asignaturas;
TRUNCATE TABLE programas;

-- Bitacora de actividad
TRUNCATE TABLE bitacora;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- OPCIONAL: borrar tambien las cuentas de prueba y dejar solo los
-- administradores. Para usarlo, quitar los dos guiones (--) del inicio
-- de la linea DELETE y volver a ejecutar solo esa linea.
-- ---------------------------------------------------------------------
-- DELETE u FROM usuarios u JOIN roles r ON r.n_idRol = u.n_idRol WHERE r.c_nombre <> 'ADMINISTRADOR';

-- Comprobacion: todo en 0 excepto usuarios, tipos y plantillas
SELECT
    (SELECT COUNT(*) FROM asignaturas)               AS asignaturas,
    (SELECT COUNT(*) FROM programas)                 AS programas,
    (SELECT COUNT(*) FROM versiones_documento)       AS documentos_cargados,
    (SELECT COUNT(*) FROM solicitudes)   AS solicitudes,
    (SELECT COUNT(*) FROM bitacora)                       AS actividad,
    (SELECT COUNT(*) FROM usuarios)                  AS usuarios_conservados,
    (SELECT COUNT(*) FROM elementos_plantilla)        AS elementos_plantilla;
