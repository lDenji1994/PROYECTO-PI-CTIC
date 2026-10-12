-- =====================================================================
--  MIGRACION 04: version del documento elegida en cada solicitud
--
--  Agrega a la tabla de detalle de solicitudes la columna
--  n_idVersionDocumento: que version (periodo) del documento de cada
--  asignatura se usa para el certificado. Vacia (NULL) = automatica,
--  como funcionaba antes.
--
--  - Ejecutar UNA vez en cada base de datos que ya exista (tu PC y la de
--    cada companero) ANTES de arrancar la version nueva del backend; si
--    no, el servidor no arranca ("missing column").
--  - Se puede ejecutar varias veces: si la columna ya existe no hace nada.
--  - No borra ni cambia datos.
--  - Si se elimina una version de documento, la solicitud vuelve a la
--    eleccion automatica (ON DELETE SET NULL).
--
--  Las bases NUEVAS no la necesitan: 01_esquema ya trae la columna.
-- =====================================================================
USE CertificadosCursosAcademicosUPB;

SET @existe := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND LOWER(TABLE_NAME) = LOWER('CertificadosCursosAcademicosUPB_DetallesSolicitudesCertificadosS')
      AND COLUMN_NAME = 'n_idVersionDocumento'
);

SET @sql := IF(@existe = 0,
    'ALTER TABLE CertificadosCursosAcademicosUPB_DetallesSolicitudesCertificadosS
         ADD COLUMN n_idVersionDocumento INT NULL,
         ADD CONSTRAINT fk_VersionesDocumentos_DetallesSolicitudes
             FOREIGN KEY (n_idVersionDocumento)
             REFERENCES CertificadosCursosAcademicosUPB_VersionesDocumentosS(n_idVersionDocumento)
             ON DELETE SET NULL',
    'SELECT ''La columna n_idVersionDocumento ya existe: no se hizo nada'' AS resultado');

PREPARE paso FROM @sql;
EXECUTE paso;
DEALLOCATE PREPARE paso;

SELECT 'Migracion 04 lista' AS resultado;
