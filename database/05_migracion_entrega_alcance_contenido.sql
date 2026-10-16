USE CertificadosCursosAcademicosUPB;

ALTER TABLE CertificadosCursosAcademicosUPB_SolicitudesCertificadosS
    MODIFY COLUMN c_estado ENUM('PENDIENTE', 'PROCESANDO', 'REALIZADO', 'ENTREGADO', 'ERROR', 'ESPERANDO_DOCUMENTOS')
        NOT NULL DEFAULT 'PENDIENTE';

DROP PROCEDURE IF EXISTS migracion05_columna;
DELIMITER //
CREATE PROCEDURE migracion05_columna(IN tabla VARCHAR(100), IN columna VARCHAR(100), IN definicion VARCHAR(500))
BEGIN
    IF (SELECT COUNT(*) FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND LOWER(TABLE_NAME) = LOWER(tabla)
          AND COLUMN_NAME = columna) = 0 THEN
        SET @sql := CONCAT('ALTER TABLE ', tabla, ' ADD COLUMN ', columna, ' ', definicion);
        PREPARE paso FROM @sql;
        EXECUTE paso;
        DEALLOCATE PREPARE paso;
    END IF;
END //
DELIMITER ;

CALL migracion05_columna('CertificadosCursosAcademicosUPB_SolicitudesCertificadosS',
    't_nombreEstudiante', 'VARCHAR(150) NULL');
CALL migracion05_columna('CertificadosCursosAcademicosUPB_SolicitudesCertificadosS',
    'dt_fechaEntrega', 'DATETIME NULL');
CALL migracion05_columna('CertificadosCursosAcademicosUPB_SolicitudesCertificadosS',
    'c_tipoContenido', 'ENUM(''RESUMIDO'', ''COMPLETO'') NOT NULL DEFAULT ''RESUMIDO''');
CALL migracion05_columna('CertificadosCursosAcademicosUPB_SolicitudesCertificadosS',
    'c_alcance', 'ENUM(''ASIGNATURAS'', ''SEMESTRES'', ''PROGRAMA'') NOT NULL DEFAULT ''ASIGNATURAS''');
CALL migracion05_columna('CertificadosCursosAcademicosUPB_SolicitudesCertificadosS',
    'n_idPrograma', 'INT NULL');
CALL migracion05_columna('CertificadosCursosAcademicosUPB_SolicitudesCertificadosS',
    't_semestres', 'VARCHAR(100) NULL');
CALL migracion05_columna('CertificadosCursosAcademicosUPB_VersionesDocumentosS',
    't_resumen', 'TEXT NULL');

DROP PROCEDURE IF EXISTS migracion05_columna;

SET @existe := (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA = DATABASE()
      AND CONSTRAINT_NAME = 'fk_Programas_SolicitudesCertificados'
);
SET @sql := IF(@existe = 0,
    'ALTER TABLE CertificadosCursosAcademicosUPB_SolicitudesCertificadosS
         ADD CONSTRAINT fk_Programas_SolicitudesCertificados
             FOREIGN KEY (n_idPrograma)
             REFERENCES CertificadosCursosAcademicosUPB_ProgramasS(n_idPrograma)
             ON DELETE SET NULL',
    'SELECT ''La llave del programa ya existe'' AS resultado');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

CREATE TABLE IF NOT EXISTS CertificadosCursosAcademicosUPB_PlanesEstudioS (
    n_idPrograma INT NOT NULL,
    n_idAsignatura INT NOT NULL,
    n_semestre INT NOT NULL,
    PRIMARY KEY (n_idPrograma, n_idAsignatura),

    CONSTRAINT fk_Programas_PlanesEstudio
        FOREIGN KEY (n_idPrograma) REFERENCES CertificadosCursosAcademicosUPB_ProgramasS(n_idPrograma)
        ON DELETE CASCADE,

    CONSTRAINT fk_Asignaturas_PlanesEstudio
        FOREIGN KEY (n_idAsignatura) REFERENCES CertificadosCursosAcademicosUPB_AsignaturasS(n_idAsignatura)
        ON DELETE CASCADE,

    CONSTRAINT chk_PlanesEstudio_semestre
        CHECK (n_semestre BETWEEN 1 AND 20)
);

SET @existe := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND LOWER(TABLE_NAME) = LOWER('CertificadosCursosAcademicosUPB_DocumentosAcademicosS')
      AND INDEX_NAME = 'idx_DocumentosAcademicos_Asignatura'
);
SET @sql := IF(@existe = 0,
    'CREATE INDEX idx_DocumentosAcademicos_Asignatura
         ON CertificadosCursosAcademicosUPB_DocumentosAcademicosS (n_idAsignatura)',
    'SELECT ''El indice por asignatura ya existe'' AS resultado');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

SET @existe := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND LOWER(TABLE_NAME) = LOWER('CertificadosCursosAcademicosUPB_DocumentosAcademicosS')
      AND INDEX_NAME = 'uq_Asignatura_TipoDocumento'
);
SET @sql := IF(@existe > 0,
    'ALTER TABLE CertificadosCursosAcademicosUPB_DocumentosAcademicosS
         DROP INDEX uq_Asignatura_TipoDocumento',
    'SELECT ''La unicidad asignatura + tipo ya no existe'' AS resultado');
PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

SELECT 'Migracion 05 lista' AS resultado;
