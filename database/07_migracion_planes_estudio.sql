USE CertificadosCursosAcademicosUPB;

DROP PROCEDURE IF EXISTS migracion07_ejecutar;
DROP PROCEDURE IF EXISTS migracion07;

DELIMITER //

CREATE PROCEDURE migracion07_ejecutar(IN sentencia TEXT)
BEGIN
    SET @sql_m07 := sentencia;
    PREPARE paso FROM @sql_m07;
    EXECUTE paso;
    DEALLOCATE PREPARE paso;
END //

CREATE PROCEDURE migracion07()
BEGIN
    DECLARE llave VARCHAR(100);

    CREATE TABLE IF NOT EXISTS planes (
        n_idPlan INT AUTO_INCREMENT PRIMARY KEY,
        n_idPrograma INT NOT NULL,
        t_nombre VARCHAR(100) NOT NULL,
        c_estado ENUM('VIGENTE', 'ANTIGUO', 'DESACTUALIZADO') NOT NULL DEFAULT 'VIGENTE',
        CONSTRAINT uq_Planes_programa_nombre UNIQUE (n_idPrograma, t_nombre),
        CONSTRAINT fk_Programas_Planes FOREIGN KEY (n_idPrograma)
            REFERENCES programas(n_idPrograma) ON DELETE CASCADE
    );

    IF (SELECT COUNT(*) FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'planes_estudio'
          AND COLUMN_NAME = 'n_idPrograma') = 1 THEN

        INSERT INTO planes (n_idPrograma, t_nombre, c_estado)
        SELECT DISTINCT pe.n_idPrograma, 'Plan de estudios', 'VIGENTE'
        FROM planes_estudio pe
        WHERE NOT EXISTS (SELECT 1 FROM planes p
                          WHERE p.n_idPrograma = pe.n_idPrograma AND p.t_nombre = 'Plan de estudios');

        IF (SELECT COUNT(*) FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'planes_estudio'
              AND COLUMN_NAME = 'n_idPlan') = 0 THEN
            CALL migracion07_ejecutar('ALTER TABLE planes_estudio ADD COLUMN n_idPlan INT NULL FIRST');
        END IF;

        UPDATE planes_estudio pe
        JOIN planes p ON p.n_idPrograma = pe.n_idPrograma AND p.t_nombre = 'Plan de estudios'
        SET pe.n_idPlan = p.n_idPlan
        WHERE pe.n_idPlan IS NULL;

        SELECT CONSTRAINT_NAME INTO llave FROM information_schema.KEY_COLUMN_USAGE
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'planes_estudio'
          AND COLUMN_NAME = 'n_idPrograma' AND REFERENCED_TABLE_NAME IS NOT NULL
        LIMIT 1;
        IF llave IS NOT NULL THEN
            CALL migracion07_ejecutar(CONCAT('ALTER TABLE planes_estudio DROP FOREIGN KEY ', llave));
        END IF;

        CALL migracion07_ejecutar('ALTER TABLE planes_estudio DROP PRIMARY KEY');
        CALL migracion07_ejecutar('ALTER TABLE planes_estudio MODIFY n_idPlan INT NOT NULL');
        CALL migracion07_ejecutar('ALTER TABLE planes_estudio ADD PRIMARY KEY (n_idPlan, n_idAsignatura)');
        CALL migracion07_ejecutar('ALTER TABLE planes_estudio DROP COLUMN n_idPrograma');
    END IF;

    IF (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'planes_estudio'
          AND CONSTRAINT_NAME = 'fk_Planes_PlanesEstudio') = 0 THEN
        CALL migracion07_ejecutar('ALTER TABLE planes_estudio ADD CONSTRAINT fk_Planes_PlanesEstudio FOREIGN KEY (n_idPlan) REFERENCES planes(n_idPlan) ON DELETE CASCADE');
    END IF;

    IF (SELECT COUNT(*) FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'planes_estudio'
          AND COLUMN_NAME = 't_plan') = 1 THEN
        CALL migracion07_ejecutar('ALTER TABLE planes_estudio DROP COLUMN t_plan');
    END IF;
    IF (SELECT COUNT(*) FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'planes_estudio'
          AND COLUMN_NAME = 'c_estadoPlan') = 1 THEN
        CALL migracion07_ejecutar('ALTER TABLE planes_estudio DROP COLUMN c_estadoPlan');
    END IF;

    IF (SELECT COUNT(*) FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'solicitudes'
          AND COLUMN_NAME = 'n_idPlan') = 0 THEN
        CALL migracion07_ejecutar('ALTER TABLE solicitudes ADD COLUMN n_idPlan INT NULL AFTER n_idPrograma');
        CALL migracion07_ejecutar('ALTER TABLE solicitudes ADD CONSTRAINT fk_Planes_Solicitudes FOREIGN KEY (n_idPlan) REFERENCES planes(n_idPlan) ON DELETE SET NULL');
        UPDATE solicitudes s
        JOIN planes p ON p.n_idPrograma = s.n_idPrograma AND p.t_nombre = 'Plan de estudios'
        SET s.n_idPlan = p.n_idPlan
        WHERE s.n_idPrograma IS NOT NULL;
    END IF;

    DELETE FROM elementos_plantilla
    WHERE c_tipoElemento = 'TEXTO'
      AND t_contenido = 'UNIVERSIDAD PONTIFICIA BOLIVARIANA';
END //

DELIMITER ;

SET @seguro_anterior := @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

CALL migracion07();

SET SQL_SAFE_UPDATES = @seguro_anterior;

DROP PROCEDURE IF EXISTS migracion07;
DROP PROCEDURE IF EXISTS migracion07_ejecutar;

SELECT 'Migracion 07 lista' AS resultado;
