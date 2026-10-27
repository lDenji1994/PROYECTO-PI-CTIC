USE CertificadosCursosAcademicosUPB;

DROP PROCEDURE IF EXISTS migracion09;
DELIMITER //
CREATE PROCEDURE migracion09(IN tabla VARCHAR(100))
BEGIN
    IF (SELECT COUNT(*) FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tabla
          AND COLUMN_NAME = 't_rutaArchivo') = 0 THEN
        SET @sql_m09 := CONCAT('ALTER TABLE ', tabla, ' ADD COLUMN t_rutaArchivo VARCHAR(500) NULL AFTER t_nombreArchivo');
        PREPARE paso FROM @sql_m09;
        EXECUTE paso;
        DEALLOCATE PREPARE paso;
    END IF;
    IF (SELECT COUNT(*) FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tabla
          AND COLUMN_NAME = 'bi_archivo') = 1 THEN
        SET @sql_m09 := CONCAT('ALTER TABLE ', tabla, ' MODIFY bi_archivo LONGBLOB NULL');
        PREPARE paso FROM @sql_m09;
        EXECUTE paso;
        DEALLOCATE PREPARE paso;
    END IF;
END //
DELIMITER ;

CALL migracion09('versiones_documento');
CALL migracion09('certificados_generados');

DROP PROCEDURE IF EXISTS migracion09;

SELECT 'Migracion 09 lista. Al arrancar, el servidor pasa los archivos a la carpeta.' AS resultado;
