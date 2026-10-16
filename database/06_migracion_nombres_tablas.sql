-- =====================================================================
--  MIGRACION 06: nombres de tablas mas faciles de entender y quitar
--  las tablas que la aplicacion no usa.
--
--  1. Renombra las tablas (los datos NO se pierden):
--
--     ANTES                                                   AHORA
--     CertificadosCursosAcademicosUPB_AsignaturasS            asignaturas
--     CertificadosCursosAcademicosUPB_ProgramasS              programas
--     CertificadosCursosAcademicosUPB_PlanesEstudioS          planes_estudio
--     CertificadosCursosAcademicosUPB_TiposDocumentosAcademicosS  tipos_documento
--     CertificadosCursosAcademicosUPB_DocumentosAcademicosS   documentos
--     CertificadosCursosAcademicosUPB_VersionesDocumentosS    versiones_documento
--     CertificadosCursosAcademicosUPB_ContenidosS             contenidos
--     CertificadosCursosAcademicosUPB_RolesS                  roles
--     CertificadosCursosAcademicosUPB_UsuariosS               usuarios
--     CertificadosCursosAcademicosUPB_TiposCertificadosS      tipos_certificado
--     CertificadosCursosAcademicosUPB_PlantillasCertificadosS plantillas
--     CertificadosCursosAcademicosUPB_VersionesPlantillasS    versiones_plantilla
--     CertificadosCursosAcademicosUPB_CamposPlantillaS        campos_plantilla
--     CertificadosCursosAcademicosUPB_ElementosPlantillaS     elementos_plantilla
--     CertificadosCursosAcademicosUPB_SolicitudesCertificadosS        solicitudes
--     CertificadosCursosAcademicosUPB_DetallesSolicitudesCertificadosS solicitud_asignaturas
--     CertificadosCursosAcademicosUPB_CertificadosGeneradosS  certificados_generados
--     CertificadosCursosAcademicosUPB_LogS                    bitacora
--
--  2. Elimina 7 tablas que ninguna pantalla llena ni usa (siempre estaban
--     vacias): ObjetivosS, CompetenciasS, CriteriosCompetenciaS,
--     MetodologiasS, EvaluacionesS, VersionesDocumentosProgramasS y
--     SeccionesPlantillaS (con la columna n_idSeccionPlantilla de los
--     campos y elementos de la plantilla).
--
--  - Ejecutar UNA vez en cada base de datos que ya exista, DESPUES de las
--    migraciones 04 y 05, y ANTES de arrancar la version nueva del backend
--    (si no, el servidor no arranca: "missing table").
--  - Se puede ejecutar varias veces: lo que ya esta hecho no se repite.
--  - Las bases NUEVAS no la necesitan: 01_esquema ya trae los nombres nuevos.
-- =====================================================================
USE CertificadosCursosAcademicosUPB;

SET @fk_anterior := @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

DROP PROCEDURE IF EXISTS migracion06_ejecutar;
DROP PROCEDURE IF EXISTS migracion06_renombrar;
DROP PROCEDURE IF EXISTS migracion06_quitar_seccion;

DELIMITER //

-- Ejecuta una sentencia armada como texto
CREATE PROCEDURE migracion06_ejecutar(IN sentencia TEXT)
BEGIN
    SET @sql_m06 := sentencia;
    PREPARE paso FROM @sql_m06;
    EXECUTE paso;
    DEALLOCATE PREPARE paso;
END //

-- Renombra solo si existe la tabla vieja y todavia no existe la nueva
CREATE PROCEDURE migracion06_renombrar(IN viejo VARCHAR(100), IN nuevo VARCHAR(100))
BEGIN
    IF (SELECT COUNT(*) FROM information_schema.TABLES
        WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = LOWER(viejo)) = 1
       AND (SELECT COUNT(*) FROM information_schema.TABLES
        WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = LOWER(nuevo)) = 0 THEN
        CALL migracion06_ejecutar(CONCAT('RENAME TABLE ', viejo, ' TO ', nuevo));
    END IF;
END //

-- Quita la llave foranea y la columna n_idSeccionPlantilla de una tabla (si las tiene)
CREATE PROCEDURE migracion06_quitar_seccion(IN tabla VARCHAR(100), IN llave VARCHAR(100))
BEGIN
    IF (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
        WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = LOWER(tabla)
          AND CONSTRAINT_NAME = llave AND CONSTRAINT_TYPE = 'FOREIGN KEY') = 1 THEN
        CALL migracion06_ejecutar(CONCAT('ALTER TABLE ', tabla, ' DROP FOREIGN KEY ', llave));
    END IF;
    IF (SELECT COUNT(*) FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = LOWER(tabla)
          AND COLUMN_NAME = 'n_idSeccionPlantilla') = 1 THEN
        CALL migracion06_ejecutar(CONCAT('ALTER TABLE ', tabla, ' DROP COLUMN n_idSeccionPlantilla'));
    END IF;
END //

DELIMITER ;

-- ---------- 1. Tablas que no se usan ----------
DROP TABLE IF EXISTS CertificadosCursosAcademicosUPB_CriteriosCompetenciaS;
DROP TABLE IF EXISTS CertificadosCursosAcademicosUPB_CompetenciasS;
DROP TABLE IF EXISTS CertificadosCursosAcademicosUPB_ObjetivosS;
DROP TABLE IF EXISTS CertificadosCursosAcademicosUPB_MetodologiasS;
DROP TABLE IF EXISTS CertificadosCursosAcademicosUPB_EvaluacionesS;
DROP TABLE IF EXISTS CertificadosCursosAcademicosUPB_VersionesDocumentosProgramasS;

-- Secciones de plantilla: primero se quita la columna que las referencia
CALL migracion06_quitar_seccion('CertificadosCursosAcademicosUPB_CamposPlantillaS', 'fk_SeccionesPlantilla_CamposPlantilla');
CALL migracion06_quitar_seccion('CertificadosCursosAcademicosUPB_ElementosPlantillaS', 'fk_SeccionesPlantilla_ElementosPlantilla');
DROP TABLE IF EXISTS CertificadosCursosAcademicosUPB_SeccionesPlantillaS;

-- ---------- 2. Nombres nuevos ----------
CALL migracion06_renombrar('CertificadosCursosAcademicosUPB_AsignaturasS', 'asignaturas');
CALL migracion06_renombrar('CertificadosCursosAcademicosUPB_ProgramasS', 'programas');
CALL migracion06_renombrar('CertificadosCursosAcademicosUPB_PlanesEstudioS', 'planes_estudio');
CALL migracion06_renombrar('CertificadosCursosAcademicosUPB_TiposDocumentosAcademicosS', 'tipos_documento');
CALL migracion06_renombrar('CertificadosCursosAcademicosUPB_DocumentosAcademicosS', 'documentos');
CALL migracion06_renombrar('CertificadosCursosAcademicosUPB_VersionesDocumentosS', 'versiones_documento');
CALL migracion06_renombrar('CertificadosCursosAcademicosUPB_ContenidosS', 'contenidos');
CALL migracion06_renombrar('CertificadosCursosAcademicosUPB_RolesS', 'roles');
CALL migracion06_renombrar('CertificadosCursosAcademicosUPB_UsuariosS', 'usuarios');
CALL migracion06_renombrar('CertificadosCursosAcademicosUPB_TiposCertificadosS', 'tipos_certificado');
CALL migracion06_renombrar('CertificadosCursosAcademicosUPB_PlantillasCertificadosS', 'plantillas');
CALL migracion06_renombrar('CertificadosCursosAcademicosUPB_VersionesPlantillasS', 'versiones_plantilla');
CALL migracion06_renombrar('CertificadosCursosAcademicosUPB_CamposPlantillaS', 'campos_plantilla');
CALL migracion06_renombrar('CertificadosCursosAcademicosUPB_ElementosPlantillaS', 'elementos_plantilla');
CALL migracion06_renombrar('CertificadosCursosAcademicosUPB_SolicitudesCertificadosS', 'solicitudes');
CALL migracion06_renombrar('CertificadosCursosAcademicosUPB_DetallesSolicitudesCertificadosS', 'solicitud_asignaturas');
CALL migracion06_renombrar('CertificadosCursosAcademicosUPB_CertificadosGeneradosS', 'certificados_generados');
CALL migracion06_renombrar('CertificadosCursosAcademicosUPB_LogS', 'bitacora');

-- ---------- 3. La bitacora guarda el nombre de la tabla afectada ----------
UPDATE bitacora SET t_nombreTabla = CASE t_nombreTabla
    WHEN 'ProgramasS' THEN 'programas'
    WHEN 'AsignaturasS' THEN 'asignaturas'
    WHEN 'DocumentosAcademicosS' THEN 'documentos'
    WHEN 'VersionesDocumentosS' THEN 'versiones_documento'
    WHEN 'SolicitudesCertificadosS' THEN 'solicitudes'
    WHEN 'DetallesSolicitudesCertificadosS' THEN 'solicitud_asignaturas'
    WHEN 'CertificadosGeneradosS' THEN 'certificados_generados'
    WHEN 'UsuariosS' THEN 'usuarios'
    WHEN 'PlanesEstudioS' THEN 'planes_estudio'
    ELSE t_nombreTabla END
WHERE n_idLog > 0;

DROP PROCEDURE IF EXISTS migracion06_ejecutar;
DROP PROCEDURE IF EXISTS migracion06_renombrar;
DROP PROCEDURE IF EXISTS migracion06_quitar_seccion;

SET FOREIGN_KEY_CHECKS = @fk_anterior;

SHOW TABLES;
SELECT 'Migracion 06 lista' AS resultado;
