-- =====================================================================
--  DATOS INICIALES PARA PROBAR EL PANEL EN LOCAL
--  Ejecutar DESPUES de 01_esquema_BASE_DE_DATOS_PI_TRACK1.sql
--
--  Es idempotente: se puede correr varias veces sin duplicar datos
--  (usa INSERT IGNORE sobre columnas UNIQUE).
--
--  SE PUEDE MODIFICAR: nombres, tipos y la plantilla de ejemplo.
--  IMPORTANTE:
--   * No hay contrasenas reales aqui. El login aun no existe; cuando se
--     implemente, la contrasena se guardara con hash (BCrypt).
--   * El usuario 'admin' (id 1) es el que usa la bitacora por defecto
--     (app.auditoria.id-usuario-por-defecto=1 en application.properties).
-- =====================================================================
USE CertificadosCursosAcademicosUPB;

-- ---------- Usuario administrador (para bitacora y solicitudes) ----------
INSERT IGNORE INTO CertificadosCursosAcademicosUPB_UsuariosS
    (t_nombreCompleto, t_usuario, t_correo, t_contrasena, b_activo, n_idRol)
SELECT 'Administrador CTIC', 'admin', NULL, 'PENDIENTE_LOGIN_SIN_CONTRASENA', TRUE, r.n_idRol
FROM CertificadosCursosAcademicosUPB_RolesS r WHERE r.c_nombre = 'ADMINISTRADOR';

INSERT IGNORE INTO CertificadosCursosAcademicosUPB_UsuariosS
    (t_nombreCompleto, t_usuario, t_correo, t_contrasena, b_activo, n_idRol)
SELECT 'Auxiliar de certificados', 'auxiliar', NULL, 'PENDIENTE_LOGIN_SIN_CONTRASENA', TRUE, r.n_idRol
FROM CertificadosCursosAcademicosUPB_RolesS r WHERE r.c_nombre = 'AUXILIAR';

-- ---------- Tipos de documento (segun las plantillas institucionales) ----------
INSERT IGNORE INTO CertificadosCursosAcademicosUPB_TiposDocumentosAcademicosS (c_codigo, t_nombre) VALUES
    ('SYLLABUS', 'Syllabus'),
    ('CARTA_DESCRIPTIVA', 'Carta Descriptiva'),
    ('CONTENIDOS', 'Contenidos programáticos'),
    ('PROGRAMA_ACADEMICO', 'Programa académico');

-- ---------- Tipo de certificado + plantilla minima para generar PDF ----------
INSERT IGNORE INTO CertificadosCursosAcademicosUPB_TiposCertificadosS (t_nombre)
VALUES ('Certificado de contenidos programáticos');

INSERT IGNORE INTO CertificadosCursosAcademicosUPB_PlantillasCertificadosS (t_nombre, n_idTipoCertificado)
SELECT 'Plantilla básica de contenidos', t.n_idTipoCertificado
FROM CertificadosCursosAcademicosUPB_TiposCertificadosS t
WHERE t.t_nombre = 'Certificado de contenidos programáticos';

INSERT IGNORE INTO CertificadosCursosAcademicosUPB_VersionesPlantillasS (c_versionFormato, n_idPlantillaCertificado)
SELECT '01', p.n_idPlantillaCertificado
FROM CertificadosCursosAcademicosUPB_PlantillasCertificadosS p
WHERE p.t_nombre = 'Plantilla básica de contenidos';

SET @version := (SELECT v.n_idVersionPlantilla
                 FROM CertificadosCursosAcademicosUPB_VersionesPlantillasS v
                 JOIN CertificadosCursosAcademicosUPB_PlantillasCertificadosS p
                   ON p.n_idPlantillaCertificado = v.n_idPlantillaCertificado
                 WHERE p.t_nombre = 'Plantilla básica de contenidos' AND v.c_versionFormato = '01');

INSERT IGNORE INTO CertificadosCursosAcademicosUPB_CamposPlantillaS
    (c_codigoCampo, t_nombreCampo, t_tipoDato, c_fuente, c_metodoObtencion, b_repetible, n_idVersionPlantilla)
VALUES
    ('ID_ESTUDIANTE', 'Identificación del estudiante', 'TEXTO', 'ESTUDIANTE', 'EXTRAER', FALSE, @version),
    ('ID_SOLICITUD', 'Número de solicitud', 'NUMERO', 'SOLICITUD', 'EXTRAER', FALSE, @version);

-- Los elementos solo se insertan si la version aun no tiene ninguno
INSERT INTO CertificadosCursosAcademicosUPB_ElementosPlantillaS
    (c_tipoElemento, t_contenido, n_posicionX, n_posicionY, n_ancho, n_alto, n_tamanoFuente, c_tipoFuente, c_alineacion, n_orden, n_idVersionPlantilla, n_idCampoPlantilla)
SELECT * FROM (
    SELECT 'TEXTO' AS tipo, 'UNIVERSIDAD PONTIFICIA BOLIVARIANA' AS contenido, 50.00 AS x, 90.00 AS y, 495.00 AS ancho, NULL AS alto, 16.00 AS fuente, 'Helvetica' AS tf, 'CENTRO' AS al, 1 AS orden, @version AS ver, NULL AS campo
    UNION ALL SELECT 'TEXTO', 'Certificado de contenidos programáticos', 50, 115, 495, NULL, 12, 'Helvetica', 'CENTRO', 2, @version, NULL
    UNION ALL SELECT 'LINEA', NULL, 50, 130, 495, NULL, NULL, NULL, NULL, 3, @version, NULL
    UNION ALL SELECT 'TEXTO', 'Se certifica la información académica registrada para:', 50, 170, 495, NULL, 11, 'Helvetica', 'IZQUIERDA', 4, @version, NULL
    UNION ALL SELECT 'CAMPO', NULL, 50, 195, 495, NULL, 11, 'Helvetica', 'IZQUIERDA', 5, @version,
        (SELECT n_idCampoPlantilla FROM CertificadosCursosAcademicosUPB_CamposPlantillaS WHERE c_codigoCampo = 'ID_ESTUDIANTE' AND n_idVersionPlantilla = @version)
    UNION ALL SELECT 'TEXTO', 'Solicitud No.', 50, 220, 100, NULL, 11, 'Helvetica', 'IZQUIERDA', 6, @version, NULL
    UNION ALL SELECT 'CAMPO', NULL, 140, 220, 200, NULL, 11, 'Helvetica', 'IZQUIERDA', 7, @version,
        (SELECT n_idCampoPlantilla FROM CertificadosCursosAcademicosUPB_CamposPlantillaS WHERE c_codigoCampo = 'ID_SOLICITUD' AND n_idVersionPlantilla = @version)
) AS nuevos
WHERE NOT EXISTS (SELECT 1 FROM CertificadosCursosAcademicosUPB_ElementosPlantillaS e WHERE e.n_idVersionPlantilla = @version);

-- ---------- Programas y asignaturas de ejemplo (codigo = MATERIA CURSO) ----------
INSERT IGNORE INTO CertificadosCursosAcademicosUPB_ProgramasS (c_codigo, t_nombre) VALUES
    ('BCSI', 'Ingeniería de Sistemas e Informática'),
    ('BCDG', 'Diseño Gráfico');

INSERT IGNORE INTO CertificadosCursosAcademicosUPB_AsignaturasS (c_codigo, t_nombre) VALUES
    ('FION 0001', 'Ondas'),
    ('ISIS 0101', 'Proyecto Integrador 1');

SELECT 'Datos iniciales cargados' AS resultado;
