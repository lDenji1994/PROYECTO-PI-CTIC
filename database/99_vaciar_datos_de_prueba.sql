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
TRUNCATE TABLE CertificadosCursosAcademicosUPB_CertificadosGeneradosS;
TRUNCATE TABLE CertificadosCursosAcademicosUPB_DetallesSolicitudesCertificadosS;
TRUNCATE TABLE CertificadosCursosAcademicosUPB_SolicitudesCertificadosS;

-- Datos del curso de cada documento cargado
TRUNCATE TABLE CertificadosCursosAcademicosUPB_CriteriosCompetenciaS;
TRUNCATE TABLE CertificadosCursosAcademicosUPB_CompetenciasS;
TRUNCATE TABLE CertificadosCursosAcademicosUPB_ObjetivosS;
TRUNCATE TABLE CertificadosCursosAcademicosUPB_ContenidosS;
TRUNCATE TABLE CertificadosCursosAcademicosUPB_MetodologiasS;
TRUNCATE TABLE CertificadosCursosAcademicosUPB_EvaluacionesS;

-- Documentos cargados (con sus archivos)
TRUNCATE TABLE CertificadosCursosAcademicosUPB_VersionesDocumentosProgramasS;
TRUNCATE TABLE CertificadosCursosAcademicosUPB_VersionesDocumentosS;
TRUNCATE TABLE CertificadosCursosAcademicosUPB_DocumentosAcademicosS;

-- Asignaturas, programas y plan de estudios
TRUNCATE TABLE CertificadosCursosAcademicosUPB_PlanesEstudioS;
TRUNCATE TABLE CertificadosCursosAcademicosUPB_AsignaturasS;
TRUNCATE TABLE CertificadosCursosAcademicosUPB_ProgramasS;

-- Bitacora de actividad
TRUNCATE TABLE CertificadosCursosAcademicosUPB_LogS;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- OPCIONAL: borrar tambien las cuentas de prueba y dejar solo los
-- administradores. Para usarlo, quitar los dos guiones (--) del inicio
-- de la linea DELETE y volver a ejecutar solo esa linea.
-- ---------------------------------------------------------------------
-- DELETE u FROM CertificadosCursosAcademicosUPB_UsuariosS u JOIN CertificadosCursosAcademicosUPB_RolesS r ON r.n_idRol = u.n_idRol WHERE r.c_nombre <> 'ADMINISTRADOR';

-- Comprobacion: todo en 0 excepto usuarios, tipos y plantillas
SELECT
    (SELECT COUNT(*) FROM CertificadosCursosAcademicosUPB_AsignaturasS)               AS asignaturas,
    (SELECT COUNT(*) FROM CertificadosCursosAcademicosUPB_ProgramasS)                 AS programas,
    (SELECT COUNT(*) FROM CertificadosCursosAcademicosUPB_VersionesDocumentosS)       AS documentos_cargados,
    (SELECT COUNT(*) FROM CertificadosCursosAcademicosUPB_SolicitudesCertificadosS)   AS solicitudes,
    (SELECT COUNT(*) FROM CertificadosCursosAcademicosUPB_LogS)                       AS actividad,
    (SELECT COUNT(*) FROM CertificadosCursosAcademicosUPB_UsuariosS)                  AS usuarios_conservados,
    (SELECT COUNT(*) FROM CertificadosCursosAcademicosUPB_ElementosPlantillaS)        AS elementos_plantilla;
