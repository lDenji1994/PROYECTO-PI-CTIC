-- =====================================================================
--  DATOS INICIALES (lo minimo para que la aplicacion funcione)
--  Ejecutar DESPUES de 01_esquema_BASE_DE_DATOS_PI_TRACK1.sql
--
--  Es idempotente: se puede correr varias veces sin duplicar datos
--  (usa INSERT IGNORE sobre columnas UNIQUE).
--
--  SE PUEDE MODIFICAR: nombres, tipos y la plantilla de ejemplo.
--  IMPORTANTE:
--   * No hay contrasenas aqui. El usuario 'admin' se crea SIN contrasena
--     valida: la primera vez que arranca el servidor se le asigna la que
--     pongas en application-local.properties
--     (app.seguridad.admin-inicial.contrasena), guardada con hash BCrypt.
--   * Las cuentas de las auxiliares NO se crean aqui: las crea el
--     administrador desde el panel (menu Usuarios).
-- =====================================================================
USE CertificadosCursosAcademicosUPB;

-- ---------- Usuario administrador (el servidor le asigna la contrasena al arrancar) ----------
INSERT IGNORE INTO usuarios
    (t_nombreCompleto, t_usuario, t_correo, t_contrasena, b_activo, n_idRol)
SELECT 'Administrador CTIC', 'admin', NULL, 'PENDIENTE_LOGIN_SIN_CONTRASENA', TRUE, r.n_idRol
FROM roles r WHERE r.c_nombre = 'ADMINISTRADOR';

-- ---------- Tipos de documento (segun las plantillas institucionales) ----------
INSERT IGNORE INTO tipos_documento (c_codigo, t_nombre) VALUES
    ('SYLLABUS', 'Syllabus'),
    ('CARTA_DESCRIPTIVA', 'Carta Descriptiva'),
    ('CONTENIDOS', 'Contenidos programáticos'),
    ('PROGRAMA_ACADEMICO', 'Programa académico');

-- ---------- Tipo de certificado + plantilla minima para generar PDF ----------
INSERT IGNORE INTO tipos_certificado (t_nombre)
VALUES ('Certificado de contenidos programáticos');

INSERT IGNORE INTO plantillas (t_nombre, n_idTipoCertificado)
SELECT 'Plantilla básica de contenidos', t.n_idTipoCertificado
FROM tipos_certificado t
WHERE t.t_nombre = 'Certificado de contenidos programáticos';

INSERT IGNORE INTO versiones_plantilla (c_versionFormato, n_idPlantillaCertificado)
SELECT '01', p.n_idPlantillaCertificado
FROM plantillas p
WHERE p.t_nombre = 'Plantilla básica de contenidos';

SET @version := (SELECT v.n_idVersionPlantilla
                 FROM versiones_plantilla v
                 JOIN plantillas p
                   ON p.n_idPlantillaCertificado = v.n_idPlantillaCertificado
                 WHERE p.t_nombre = 'Plantilla básica de contenidos' AND v.c_versionFormato = '01');

INSERT IGNORE INTO campos_plantilla
    (c_codigoCampo, t_nombreCampo, t_tipoDato, c_fuente, c_metodoObtencion, b_repetible, n_idVersionPlantilla)
VALUES
    ('ID_ESTUDIANTE', 'Identificación del estudiante', 'TEXTO', 'ESTUDIANTE', 'EXTRAER', FALSE, @version),
    ('ID_SOLICITUD', 'Número de solicitud', 'NUMERO', 'SOLICITUD', 'EXTRAER', FALSE, @version);

-- Los elementos solo se insertan si la version aun no tiene ninguno
INSERT INTO elementos_plantilla
    (c_tipoElemento, t_contenido, n_posicionX, n_posicionY, n_ancho, n_alto, n_tamanoFuente, c_tipoFuente, c_alineacion, n_orden, n_idVersionPlantilla, n_idCampoPlantilla)
SELECT * FROM (
    SELECT 'TEXTO' AS tipo, 'UNIVERSIDAD PONTIFICIA BOLIVARIANA' AS contenido, 50.00 AS x, 90.00 AS y, 495.00 AS ancho, NULL AS alto, 16.00 AS fuente, 'Helvetica' AS tf, 'CENTRO' AS al, 1 AS orden, @version AS ver, NULL AS campo
    UNION ALL SELECT 'TEXTO', 'Certificado de contenidos programáticos', 50, 115, 495, NULL, 12, 'Helvetica', 'CENTRO', 2, @version, NULL
    UNION ALL SELECT 'LINEA', NULL, 50, 130, 495, NULL, NULL, NULL, NULL, 3, @version, NULL
    UNION ALL SELECT 'TEXTO', 'Se certifica la información académica registrada para:', 50, 170, 495, NULL, 11, 'Helvetica', 'IZQUIERDA', 4, @version, NULL
    UNION ALL SELECT 'CAMPO', NULL, 50, 195, 495, NULL, 11, 'Helvetica', 'IZQUIERDA', 5, @version,
        (SELECT n_idCampoPlantilla FROM campos_plantilla WHERE c_codigoCampo = 'ID_ESTUDIANTE' AND n_idVersionPlantilla = @version)
    UNION ALL SELECT 'TEXTO', 'Solicitud No.', 50, 220, 100, NULL, 11, 'Helvetica', 'IZQUIERDA', 6, @version, NULL
    UNION ALL SELECT 'CAMPO', NULL, 140, 220, 200, NULL, 11, 'Helvetica', 'IZQUIERDA', 7, @version,
        (SELECT n_idCampoPlantilla FROM campos_plantilla WHERE c_codigoCampo = 'ID_SOLICITUD' AND n_idVersionPlantilla = @version)
) AS nuevos
WHERE NOT EXISTS (SELECT 1 FROM elementos_plantilla e WHERE e.n_idVersionPlantilla = @version);

-- Programas y asignaturas: NO se crean aqui. Se registran desde el panel
-- (Contenido de cursos), asi la base arranca vacia y lista para usarse.

SELECT 'Datos iniciales cargados' AS resultado;
