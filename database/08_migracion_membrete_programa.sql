USE CertificadosCursosAcademicosUPB;

DROP PROCEDURE IF EXISTS migracion08_columna;
DELIMITER //
CREATE PROCEDURE migracion08_columna(IN tabla VARCHAR(100), IN columna VARCHAR(100), IN definicion VARCHAR(500))
BEGIN
    IF (SELECT COUNT(*) FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND LOWER(TABLE_NAME) = LOWER(tabla)
          AND COLUMN_NAME = columna) = 0 THEN
        SET @sql_m08 := CONCAT('ALTER TABLE ', tabla, ' ADD COLUMN ', columna, ' ', definicion);
        PREPARE paso FROM @sql_m08;
        EXECUTE paso;
        DEALLOCATE PREPARE paso;
    END IF;
END //
DELIMITER ;

CALL migracion08_columna('programas', 't_escuela', 'VARCHAR(150) NULL');
CALL migracion08_columna('programas', 't_facultad', 'VARCHAR(150) NULL');
CALL migracion08_columna('solicitudes', 't_escuela', 'VARCHAR(150) NULL');
CALL migracion08_columna('solicitudes', 't_facultad', 'VARCHAR(150) NULL');

DROP PROCEDURE IF EXISTS migracion08_columna;

SELECT 'Migracion 08 lista' AS resultado;
