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
