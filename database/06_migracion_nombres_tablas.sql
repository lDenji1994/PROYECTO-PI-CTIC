USE CertificadosCursosAcademicosUPB;

SET @fk_anterior := @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

DROP PROCEDURE IF EXISTS migracion06_ejecutar;
DROP PROCEDURE IF EXISTS migracion06_renombrar;
DROP PROCEDURE IF EXISTS migracion06_quitar_seccion;

DELIMITER //

CREATE PROCEDURE migracion06_ejecutar(IN sentencia TEXT)
BEGIN
    SET @sql_m06 := sentencia;
    PREPARE paso FROM @sql_m06;
    EXECUTE paso;
    DEALLOCATE PREPARE paso;
END //

CREATE PROCEDURE migracion06_renombrar(IN viejo VARCHAR(100), IN nuevo VARCHAR(100))
BEGIN
    IF (SELECT COUNT(*) FROM information_schema.TABLES
        WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = LOWER(viejo)) = 1
       AND (SELECT COUNT(*) FROM information_schema.TABLES
        WHERE TABLE_SCHEMA = DATABASE() AND LOWER(TABLE_NAME) = LOWER(nuevo)) = 0 THEN
        CALL migracion06_ejecutar(CONCAT('RENAME TABLE ', viejo, ' TO ', nuevo));
    END IF;
END //

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

DROP TABLE IF EXISTS CertificadosCursosAcademicosUPB_CriteriosCompetenciaS;
DROP TABLE IF EXISTS CertificadosCursosAcademicosUPB_CompetenciasS;
DROP TABLE IF EXISTS CertificadosCursosAcademicosUPB_ObjetivosS;
DROP TABLE IF EXISTS CertificadosCursosAcademicosUPB_MetodologiasS;
DROP TABLE IF EXISTS CertificadosCursosAcademicosUPB_EvaluacionesS;
DROP TABLE IF EXISTS CertificadosCursosAcademicosUPB_VersionesDocumentosProgramasS;

CALL migracion06_quitar_seccion('CertificadosCursosAcademicosUPB_CamposPlantillaS', 'fk_SeccionesPlantilla_CamposPlantilla');
CALL migracion06_quitar_seccion('CertificadosCursosAcademicosUPB_ElementosPlantillaS', 'fk_SeccionesPlantilla_ElementosPlantilla');
DROP TABLE IF EXISTS CertificadosCursosAcademicosUPB_SeccionesPlantillaS;

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
