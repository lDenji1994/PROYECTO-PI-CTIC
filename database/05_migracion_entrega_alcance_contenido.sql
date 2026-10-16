-- =====================================================================
--  NOTA: solo para bases creadas ANTES de la migracion 06 (usa los
--  nombres de tabla viejos). Las bases nuevas no la necesitan.
--  MIGRACION 05: entrega del certificado, nombre del estudiante,
--  tipo de contenido (resumido / completo), alcance de la solicitud
--  (asignaturas, semestres o programa) y plan de estudios.
--
--  Cambios:
--   1. SolicitudesCertificadosS
--        - estado nuevo ENTREGADO (el certificado ya se entrego al estudiante)
--        - t_nombreEstudiante  : nombre del estudiante (ademas del ID)
--        - dt_fechaEntrega     : cuando se marco como entregado
--        - c_tipoContenido     : RESUMIDO (un parrafo por asignatura) o
--                                COMPLETO (todo el contenido, varias paginas)
--        - c_alcance           : ASIGNATURAS, SEMESTRES o PROGRAMA
--        - n_idPrograma        : programa de la solicitud (semestres / programa)
--        - t_semestres         : semestres pedidos, ej. "1,2,5"
--   2. VersionesDocumentosS
--        - t_resumen           : parrafo breve para el certificado resumido
--                                (si esta vacio se usa la descripcion)
--   3. Tabla nueva PlanesEstudioS: en que semestre de cada programa va
--      cada asignatura. Con ella se arma una solicitud por semestre o por
--      programa completo.
--   4. DocumentosAcademicosS: se quita la regla "un solo documento por
--      asignatura y tipo". Asi una asignatura puede tener varios
--      documentos, incluso del mismo tipo (cada archivo con su formato).
--
--  - Ejecutar UNA vez en cada base de datos que ya exista (tu PC y la de
--    cada companero) ANTES de arrancar la version nueva del backend; si
--    no, el servidor no arranca ("missing column" / "missing table").
--  - Requiere haber ejecutado antes la migracion 04.
--  - Se puede ejecutar varias veces: lo que ya existe no se toca.
--  - No borra ni cambia datos: las solicitudes viejas quedan como
--    RESUMIDO / ASIGNATURAS y sin nombre (se puede escribir despues).
--
--  Las bases NUEVAS no la necesitan: 01_esquema ya trae todo.
-- =====================================================================
USE CertificadosCursosAcademicosUPB;

-- ---------- 1. Estado ENTREGADO (MODIFY es seguro de repetir) ----------
ALTER TABLE CertificadosCursosAcademicosUPB_SolicitudesCertificadosS
    MODIFY COLUMN c_estado ENUM('PENDIENTE', 'PROCESANDO', 'REALIZADO', 'ENTREGADO', 'ERROR', 'ESPERANDO_DOCUMENTOS')
        NOT NULL DEFAULT 'PENDIENTE';

-- ---------- Columnas nuevas: se agregan solo si no existen ----------
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

-- ---------- Llave foranea del programa de la solicitud ----------
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

-- ---------- 3. Plan de estudios ----------
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

-- ---------- 4. Varios documentos por asignatura ----------
-- Primero un indice normal por asignatura (lo necesita su llave foranea),
-- despues se quita la unicidad asignatura + tipo.
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
