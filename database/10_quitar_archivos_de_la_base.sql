USE CertificadosCursosAcademicosUPB;

SELECT
    (SELECT COUNT(*) FROM versiones_documento WHERE t_rutaArchivo IS NULL OR t_rutaArchivo = '') AS documentos_sin_ruta,
    (SELECT COUNT(*) FROM certificados_generados WHERE t_rutaArchivo IS NULL OR t_rutaArchivo = '') AS certificados_sin_ruta;

DROP PROCEDURE IF EXISTS migracion10;
DELIMITER //
CREATE PROCEDURE migracion10(IN tabla VARCHAR(100))
BEGIN
    DECLARE pendientes INT;
    SET @sql_m10 := CONCAT('SELECT COUNT(*) INTO @pendientes_m10 FROM ', tabla,
                           ' WHERE t_rutaArchivo IS NULL OR t_rutaArchivo = ''''');
    PREPARE paso FROM @sql_m10;
    EXECUTE paso;
    DEALLOCATE PREPARE paso;
    SET pendientes = @pendientes_m10;

    IF pendientes > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Hay archivos que aun no estan en la carpeta. Arranca el servidor una vez y vuelve a correr este script.';
    END IF;

    IF (SELECT COUNT(*) FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tabla
          AND COLUMN_NAME = 'bi_archivo') = 1 THEN
        SET @sql_m10 := CONCAT('ALTER TABLE ', tabla, ' DROP COLUMN bi_archivo');
        PREPARE paso FROM @sql_m10;
        EXECUTE paso;
        DEALLOCATE PREPARE paso;
    END IF;
    SET @sql_m10 := CONCAT('ALTER TABLE ', tabla, ' MODIFY t_rutaArchivo VARCHAR(500) NOT NULL');
    PREPARE paso FROM @sql_m10;
    EXECUTE paso;
    DEALLOCATE PREPARE paso;
END //
DELIMITER ;

CALL migracion10('versiones_documento');
CALL migracion10('certificados_generados');

DROP PROCEDURE IF EXISTS migracion10;

OPTIMIZE TABLE versiones_documento, certificados_generados;

SELECT TABLE_NAME AS tabla, ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 2) AS tamano_mb
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME IN ('versiones_documento', 'certificados_generados');
