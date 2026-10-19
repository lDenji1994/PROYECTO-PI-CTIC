USE CertificadosCursosAcademicosUPB;

DROP PROCEDURE IF EXISTS migracion07_columna;
DELIMITER //
CREATE PROCEDURE migracion07_columna(IN tabla VARCHAR(100), IN columna VARCHAR(100), IN definicion VARCHAR(500))
BEGIN
    IF (SELECT COUNT(*) FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND LOWER(TABLE_NAME) = LOWER(tabla)
          AND COLUMN_NAME = columna) = 0 THEN
        SET @sql_m07 := CONCAT('ALTER TABLE ', tabla, ' ADD COLUMN ', columna, ' ', definicion);
        PREPARE paso FROM @sql_m07;
        EXECUTE paso;
        DEALLOCATE PREPARE paso;
    END IF;
END //
DELIMITER ;

CALL migracion07_columna('planes_estudio', 't_plan', 'VARCHAR(60) NULL');
CALL migracion07_columna('planes_estudio', 'c_estadoPlan',
    'ENUM(''VIGENTE'', ''ANTIGUO'', ''DESACTUALIZADO'') NOT NULL DEFAULT ''VIGENTE''');

DROP PROCEDURE IF EXISTS migracion07_columna;

DELETE FROM elementos_plantilla
WHERE c_tipoElemento = 'TEXTO'
  AND t_contenido = 'UNIVERSIDAD PONTIFICIA BOLIVARIANA';

SELECT 'Migracion 07 lista' AS resultado;
