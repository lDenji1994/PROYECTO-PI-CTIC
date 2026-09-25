-- ============================================================
--        NORMAS Y CONVENCIONES DE LA BASE DE DATOS
--        CERTIFICADOS DE CURSOS ACADÉMICOS UPB
-- ============================================================
--
-- 1. NOMBRE DE LA APLICACIÓN
--    Nombre funcional: CERTIFICADOS DE CURSOS ACADÉMICOS UPB
--
--    Nombre utilizado en SQL: CertificadosCursosAcademicosUPB
--
--
-- 2. CONVENCIÓN PARA NOMBRES DE TABLAS
--    Formato:   NombreDeLaApp_NombreDeLaTablaS
--
--    La letra "S" al final indica que se trata de una tabla.
--
--
-- 3. CONVENCIÓN PARA IDENTIFICADORES
--    Todo identificador de tabla debe:
--      - comenzar con "n_"
--      - ser de tipo INT
--      - ser AUTO_INCREMENT
--      - funcionar como PRIMARY KEY
--
--
-- 4. PREFIJOS PARA ATRIBUTOS
--
--    n_  = valor numérico / identificador
--    t_  = texto
--    c_  = código
--    b_  = booleano
--    d_  = fecha
--    dt_ = fecha y hora
--    bi_ = información binaria
--
--
-- 5. CLAVES FORÁNEAS
--    Las claves foráneas deben utilizar la siguiente sintaxis:
--
--    fk_NombreTablaPrincipal_NombreTablaForanea
--
--
-- 6. AUDITORÍA
--    La base de datos debe contar siempre con una tabla LOG
--    para registrar las operaciones realizadas dentro del
--    sistema.
--
--    La tabla LOG debe almacenar como mínimo:
--
--      - n_idLog       : identificador de la operación
--      - t_ip          : dirección IP desde la cual se realizó
--                       la operación
--      - n_idUsuario   : identificador del usuario que realizó
--                       la operación
--      - t_nombreTabla : nombre de la tabla afectada
--      - t_nombreProceso : nombre del proceso ejecutado
--      - dt_fechaInicio : fecha y hora de inicio
--      - dt_fechaFin    : fecha y hora de finalización
--
-- ============================================================
CREATE DATABASE CertificadosCursosAcademicosUPB
CHARACTER SET utf8mb4
COLLATE UTF8MB4_UNICODE_CI;

USE CertificadosCursosAcademicosUPB;

-- ========================================================================================================================
--                     1. MÓDULO ACADÉMICO
-- ========================================================================================================================


-- ============================================================
--                     ASIGNATURAS
-- ============================================================
CREATE TABLE CertificadosCursosAcademicosUPB_AsignaturasS (
    n_idAsignatura INT AUTO_INCREMENT PRIMARY KEY,
    c_codigo VARCHAR(20) UNIQUE NOT NULL,
    t_nombre VARCHAR(150) NOT NULL
);


-- ============================================================
--              TIPOS DE DOCUMENTOS ACADÉMICOS
-- ============================================================
CREATE TABLE CertificadosCursosAcademicosUPB_TiposDocumentosAcademicosS (
    n_idTipoDocumentoAcademico INT AUTO_INCREMENT PRIMARY KEY,
    c_codigo VARCHAR(50) UNIQUE NOT NULL,
    t_nombre VARCHAR(150) UNIQUE NOT NULL
);


-- ============================================================
--                DOCUMENTOS ACADÉMICOS
-- ============================================================
CREATE TABLE CertificadosCursosAcademicosUPB_DocumentosAcademicosS (
    n_idDocumentoAcademico INT AUTO_INCREMENT PRIMARY KEY,
    c_codigoFormato VARCHAR(50) NULL,
    c_versionFormato VARCHAR(20) NULL,
    n_idAsignatura INT NOT NULL,
    n_idTipoDocumentoAcademico INT NOT NULL,

    CONSTRAINT fk_Asignaturas_DocumentosAcademicos
        FOREIGN KEY (n_idAsignatura) REFERENCES CertificadosCursosAcademicosUPB_AsignaturasS(n_idAsignatura)
        ON DELETE RESTRICT,

    CONSTRAINT fk_TiposDocumentosAcademicos_DocumentosAcademicos
        FOREIGN KEY (n_idTipoDocumentoAcademico) REFERENCES CertificadosCursosAcademicosUPB_TiposDocumentosAcademicosS(n_idTipoDocumentoAcademico)
        ON DELETE RESTRICT,

    CONSTRAINT uq_Asignatura_TipoDocumento
        UNIQUE (n_idAsignatura, n_idTipoDocumentoAcademico)
);


-- ============================================================
--                         PROGRAMAS
-- ============================================================
CREATE TABLE CertificadosCursosAcademicosUPB_ProgramasS (
    n_idPrograma INT AUTO_INCREMENT PRIMARY KEY,
    c_codigo VARCHAR(50) UNIQUE NULL,
    t_nombre VARCHAR(200) NOT NULL
);


-- ============================================================
--                  VERSIONES DOCUMENTOS
-- ============================================================
CREATE TABLE CertificadosCursosAcademicosUPB_VersionesDocumentosS (
    n_idVersionDocumento INT AUTO_INCREMENT PRIMARY KEY,
    n_idDocumentoAcademico INT NOT NULL,
    c_periodo VARCHAR(20) NULL,
    
    -- ========================================================
    -- Archivo original
    -- ========================================================
    
    t_nombreArchivo VARCHAR(255) NOT NULL,
    bi_archivo LONGBLOB NOT NULL,

    -- ========================================================
    -- Identificación académica
    -- ========================================================

    t_escuela VARCHAR(150) NULL,
    t_facultad VARCHAR(150) NULL,

    -- ========================================================
    -- Clasificación académica
    -- ========================================================

    c_clasificacionCINE VARCHAR(100) NULL,
    t_nucleoBasicoConocimiento TEXT NULL,

    -- ========================================================
    -- Formación
    -- ========================================================

    c_ciclo VARCHAR(100) NULL,
    c_nivelFormacion VARCHAR(100) NULL,

    -- ========================================================
    -- Intensidad horaria
    -- ========================================================

    n_horasTeoricas DECIMAL(6,2) NULL,
    n_horasPracticas DECIMAL(6,2) NULL,
    n_horasLaboratorio DECIMAL(6,2) NULL,
    n_horasIndependientes DECIMAL(6,2) NULL,

    n_creditos DECIMAL(5,2) NULL,

    -- ========================================================
    -- Información académica general
    -- ========================================================

    t_requisitos TEXT NULL,
    t_justificacion TEXT NULL,
    t_descripcion TEXT NULL,
    t_proposito TEXT NULL,

    -- ========================================================
    -- Información propia de Carta Descriptiva
    -- ========================================================

    c_modoCalificacion VARCHAR(100) NULL,
    t_modalidades TEXT NULL,

    -- ========================================================
    -- Información administrativa
    -- ========================================================

    t_actaAprobacion TEXT NULL,
    t_actaModificacion TEXT NULL,
    t_observaciones TEXT NULL,

    -- ========================================================
    -- Programa que diseña el curso
    -- ========================================================

    n_idProgramaDisena INT NULL,

    -- ========================================================
    -- Control de carga
    -- ========================================================

    dt_fechaCarga DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_DocumentosAcademicos_VersionesDocumentos
        FOREIGN KEY (n_idDocumentoAcademico) REFERENCES CertificadosCursosAcademicosUPB_DocumentosAcademicosS(n_idDocumentoAcademico)
        ON DELETE RESTRICT,

    CONSTRAINT fk_VersionesDocumentos_ProgramaDisena
        FOREIGN KEY (n_idProgramaDisena) REFERENCES CertificadosCursosAcademicosUPB_ProgramasS(n_idPrograma)
        ON DELETE RESTRICT
);


-- ============================================================
--       PROGRAMAS QUE PUEDEN TOMAR EL CURSO
-- ============================================================
CREATE TABLE CertificadosCursosAcademicosUPB_VersionesDocumentosProgramasS (
    n_idVersionDocumento INT NOT NULL,
    n_idPrograma INT NOT NULL,
    PRIMARY KEY (n_idVersionDocumento, n_idPrograma),

    CONSTRAINT fk_VersionesDocumentosProgramas_Version
        FOREIGN KEY (n_idVersionDocumento) REFERENCES CertificadosCursosAcademicosUPB_VersionesDocumentosS(n_idVersionDocumento)
        ON DELETE RESTRICT,

    CONSTRAINT fk_VersionesDocumentosProgramas_Programa
        FOREIGN KEY (n_idPrograma) REFERENCES CertificadosCursosAcademicosUPB_ProgramasS(n_idPrograma)
        ON DELETE RESTRICT
);


-- ============================================================
--                         OBJETIVOS
-- ============================================================
CREATE TABLE CertificadosCursosAcademicosUPB_ObjetivosS (
    n_idObjetivo INT AUTO_INCREMENT PRIMARY KEY,
    t_descripcion TEXT NOT NULL,
    n_idVersionDocumento INT NOT NULL,

    CONSTRAINT fk_VersionesDocumentos_Objetivos
        FOREIGN KEY (n_idVersionDocumento) REFERENCES CertificadosCursosAcademicosUPB_VersionesDocumentosS(n_idVersionDocumento)
        ON DELETE RESTRICT
);


-- ============================================================
--                        CONTENIDOS
-- ============================================================
CREATE TABLE CertificadosCursosAcademicosUPB_ContenidosS (
    n_idContenido INT AUTO_INCREMENT PRIMARY KEY,
    n_semana INT NULL,
    t_unidad VARCHAR(150) NULL,
    t_tema VARCHAR(250) NULL,
    t_descripcion TEXT NOT NULL,
    n_orden INT NULL,
    n_idVersionDocumento INT NOT NULL,

    CONSTRAINT fk_VersionesDocumentos_Contenidos
        FOREIGN KEY (n_idVersionDocumento) REFERENCES CertificadosCursosAcademicosUPB_VersionesDocumentosS(n_idVersionDocumento)
        ON DELETE RESTRICT,

    CONSTRAINT chk_Contenidos_Semana
        CHECK (n_semana IS NULL OR n_semana > 0),

    CONSTRAINT chk_Contenidos_Orden
        CHECK (n_orden IS NULL OR n_orden > 0)
);


-- ============================================================
--                       COMPETENCIAS
-- ============================================================
CREATE TABLE CertificadosCursosAcademicosUPB_CompetenciasS (
    n_idCompetencia INT AUTO_INCREMENT PRIMARY KEY,
    t_descripcion TEXT NOT NULL,
    n_orden INT NULL,
    n_idVersionDocumento INT NOT NULL,

    CONSTRAINT fk_VersionesDocumentos_Competencias
        FOREIGN KEY (n_idVersionDocumento) REFERENCES CertificadosCursosAcademicosUPB_VersionesDocumentosS(n_idVersionDocumento)
        ON DELETE RESTRICT,

    CONSTRAINT chk_Competencias_Orden
        CHECK (n_orden IS NULL OR n_orden > 0)
);


-- ============================================================
--                 CRITERIOS DE COMPETENCIA
-- ============================================================
CREATE TABLE CertificadosCursosAcademicosUPB_CriteriosCompetenciaS (
    n_idCriterioCompetencia INT AUTO_INCREMENT PRIMARY KEY,
    t_descripcion TEXT NOT NULL,
    n_idCompetencia INT NOT NULL,

    CONSTRAINT fk_Competencias_CriteriosCompetencia
        FOREIGN KEY (n_idCompetencia) REFERENCES CertificadosCursosAcademicosUPB_CompetenciasS(n_idCompetencia)
        ON DELETE RESTRICT
);


-- ============================================================
--                       METODOLOGÍAS
-- ============================================================
CREATE TABLE CertificadosCursosAcademicosUPB_MetodologiasS (
    n_idMetodologia INT AUTO_INCREMENT PRIMARY KEY,
    t_descripcion TEXT NOT NULL,
    n_orden INT NULL,
    n_idVersionDocumento INT NOT NULL,

    CONSTRAINT fk_VersionesDocumentos_Metodologias
        FOREIGN KEY (n_idVersionDocumento) REFERENCES CertificadosCursosAcademicosUPB_VersionesDocumentosS(n_idVersionDocumento)
        ON DELETE RESTRICT,

    CONSTRAINT chk_Metodologias_Orden
        CHECK (n_orden IS NULL OR n_orden > 0)
);


-- ============================================================
--                       EVALUACIONES
-- ============================================================
CREATE TABLE CertificadosCursosAcademicosUPB_EvaluacionesS (
    n_idEvaluacion INT AUTO_INCREMENT PRIMARY KEY,
    t_tipo VARCHAR(100) NULL,
    t_descripcion TEXT NOT NULL,
    n_porcentaje DECIMAL(5,2) NULL,
    n_orden INT NULL,
    n_idVersionDocumento INT NOT NULL,

    CONSTRAINT fk_VersionesDocumentos_Evaluaciones
        FOREIGN KEY (n_idVersionDocumento) REFERENCES CertificadosCursosAcademicosUPB_VersionesDocumentosS(n_idVersionDocumento)
        ON DELETE RESTRICT,

    CONSTRAINT chk_Evaluaciones_Porcentaje
        CHECK (n_porcentaje IS NULL OR (n_porcentaje >= 0 AND n_porcentaje <= 100)),

    CONSTRAINT chk_Evaluaciones_Orden
        CHECK (n_orden IS NULL OR n_orden > 0)
);

-- ========================================================================================================================
--             2. MÓDULO DE ROLES Y USUARIOS
-- ========================================================================================================================
CREATE TABLE CertificadosCursosAcademicosUPB_RolesS (
    n_idRol INT AUTO_INCREMENT PRIMARY KEY,
    c_nombre VARCHAR(50) UNIQUE NOT NULL,
    t_descripcion VARCHAR(150) NULL
);

CREATE TABLE CertificadosCursosAcademicosUPB_UsuariosS (
    n_idUsuario INT AUTO_INCREMENT PRIMARY KEY,
    t_nombreCompleto VARCHAR(150) NOT NULL,
    t_usuario VARCHAR(100) UNIQUE NOT NULL,
    t_correo VARCHAR(150) UNIQUE NULL,
    t_contrasena VARCHAR(255) NOT NULL,   #la contraseña no se almacenara en texto plano, se almacenara el resultado despues de aplicar hash
    b_activo BOOLEAN NOT NULL DEFAULT TRUE,
    n_idRol INT NOT NULL,

    CONSTRAINT fk_Roles_Usuarios
        FOREIGN KEY (n_idRol) REFERENCES CertificadosCursosAcademicosUPB_RolesS(n_idRol)
        ON DELETE RESTRICT
);

INSERT INTO CertificadosCursosAcademicosUPB_RolesS (c_nombre, t_descripcion)
VALUES
    ('ADMINISTRADOR',
     'Administrador de la aplicación perteneciente a CTIC UPB'),
    ('AUXILIAR',
     'Usuario encargado de gestionar solicitudes y certificados');


-- ========================================================================================================================
--            3. MÓDULO DE CERTIFICADOS Y TEMPLATES
-- ========================================================================================================================
CREATE TABLE CertificadosCursosAcademicosUPB_TiposCertificadosS (   
    n_idTipoCertificado INT AUTO_INCREMENT PRIMARY KEY,
    t_nombre VARCHAR(150) UNIQUE NOT NULL
);

CREATE TABLE CertificadosCursosAcademicosUPB_PlantillasCertificadosS (   
    n_idPlantillaCertificado INT AUTO_INCREMENT PRIMARY KEY,
    t_nombre VARCHAR(150) UNIQUE NOT NULL,
    n_idTipoCertificado INT NOT NULL,

    CONSTRAINT fk_TiposCertificados_PlantillasCertificados
        FOREIGN KEY (n_idTipoCertificado) REFERENCES CertificadosCursosAcademicosUPB_TiposCertificadosS(n_idTipoCertificado)
        ON DELETE RESTRICT
);

CREATE TABLE CertificadosCursosAcademicosUPB_VersionesPlantillasS (   
    n_idVersionPlantilla INT AUTO_INCREMENT PRIMARY KEY,
    c_versionFormato VARCHAR(20) NOT NULL,
    dt_fechaCarga DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    n_idPlantillaCertificado INT NOT NULL,

    CONSTRAINT fk_PlantillasCertificados_VersionesPlantillas
        FOREIGN KEY (n_idPlantillaCertificado) REFERENCES CertificadosCursosAcademicosUPB_PlantillasCertificadosS(n_idPlantillaCertificado)
        ON DELETE RESTRICT,

    CONSTRAINT uq_Plantilla_Version
        UNIQUE (n_idPlantillaCertificado, c_versionFormato)
);

CREATE TABLE CertificadosCursosAcademicosUPB_SeccionesPlantillaS (   
    n_idSeccionPlantilla INT AUTO_INCREMENT PRIMARY KEY,
    c_codigoSeccion VARCHAR(100) NOT NULL,
    t_nombreSeccion VARCHAR(150) NOT NULL,
    b_repetible BOOLEAN NOT NULL DEFAULT FALSE,
    n_orden INT NULL,
    n_idVersionPlantilla INT NOT NULL,

    CONSTRAINT fk_VersionesPlantillas_SeccionesPlantilla
        FOREIGN KEY (n_idVersionPlantilla) REFERENCES CertificadosCursosAcademicosUPB_VersionesPlantillasS(n_idVersionPlantilla)
        ON DELETE RESTRICT,

    CONSTRAINT uq_VersionPlantilla_CodigoSeccion
        UNIQUE (n_idVersionPlantilla, c_codigoSeccion),

    CONSTRAINT chk_SeccionesPlantilla_Orden
        CHECK (n_orden IS NULL OR n_orden > 0)
);

CREATE TABLE CertificadosCursosAcademicosUPB_CamposPlantillaS (   
    n_idCampoPlantilla INT AUTO_INCREMENT PRIMARY KEY,
    c_codigoCampo VARCHAR(100) NOT NULL,
    t_nombreCampo VARCHAR(150) NOT NULL,
    t_tipoDato ENUM('TEXTO', 'NUMERO', 'FECHA', 'BOOLEANO') NOT NULL,
    c_fuente VARCHAR(50) NOT NULL,
    c_metodoObtencion ENUM('EXTRAER', 'CALCULAR', 'TRANSFORMAR') NOT NULL,
    b_repetible BOOLEAN NOT NULL DEFAULT FALSE,
    t_reglaObtencion TEXT NULL,
    n_idVersionPlantilla INT NOT NULL,
    n_idSeccionPlantilla INT NULL,

    CONSTRAINT fk_VersionesPlantillas_CamposPlantilla
        FOREIGN KEY (n_idVersionPlantilla) REFERENCES CertificadosCursosAcademicosUPB_VersionesPlantillasS(n_idVersionPlantilla)
        ON DELETE RESTRICT,
        
    CONSTRAINT fk_SeccionesPlantilla_CamposPlantilla
        FOREIGN KEY (n_idSeccionPlantilla) REFERENCES CertificadosCursosAcademicosUPB_SeccionesPlantillaS(n_idSeccionPlantilla)
        ON DELETE RESTRICT,

    CONSTRAINT uq_VersionPlantilla_CodigoCampo
        UNIQUE (n_idVersionPlantilla, c_codigoCampo)
);

CREATE TABLE CertificadosCursosAcademicosUPB_ElementosPlantillaS (
    n_idElementoPlantilla INT AUTO_INCREMENT PRIMARY KEY,
    c_tipoElemento ENUM('TEXTO', 'IMAGEN', 'CAMPO', 'LINEA', 'FIRMA', 'TABLA') NOT NULL,
    t_contenido TEXT NULL,
    n_posicionX DECIMAL(8,2) NULL,
    n_posicionY DECIMAL(8,2) NULL,
    n_ancho DECIMAL(8,2) NULL,
    n_alto DECIMAL(8,2) NULL,
    n_tamanoFuente DECIMAL(5,2) NULL,
    c_tipoFuente VARCHAR(100) NULL,
    c_alineacion VARCHAR(20) NULL,
    n_orden INT NULL,
    n_idVersionPlantilla INT NOT NULL,
    n_idSeccionPlantilla INT NULL,
    n_idCampoPlantilla INT NULL,

    CONSTRAINT fk_VersionesPlantillas_ElementosPlantilla
        FOREIGN KEY (n_idVersionPlantilla) REFERENCES CertificadosCursosAcademicosUPB_VersionesPlantillasS(n_idVersionPlantilla)
        ON DELETE RESTRICT,

    CONSTRAINT fk_SeccionesPlantilla_ElementosPlantilla
        FOREIGN KEY (n_idSeccionPlantilla) REFERENCES CertificadosCursosAcademicosUPB_SeccionesPlantillaS(n_idSeccionPlantilla)
        ON DELETE RESTRICT,
        
    CONSTRAINT fk_CamposPlantilla_ElementosPlantilla
        FOREIGN KEY (n_idCampoPlantilla) REFERENCES CertificadosCursosAcademicosUPB_CamposPlantillaS(n_idCampoPlantilla)
        ON DELETE RESTRICT,
        
    CONSTRAINT chk_ElementosPlantilla_Orden
    CHECK (n_orden IS NULL OR n_orden > 0),
    
    CONSTRAINT chk_ElementosPlantilla_Campo
    CHECK (c_tipoElemento <> 'CAMPO' OR n_idCampoPlantilla IS NOT NULL)
);

-- ========================================================================================================================
--            4. MÓDULO DE SOLICITUDES Y GENERACIÓN
-- ========================================================================================================================
CREATE TABLE CertificadosCursosAcademicosUPB_SolicitudesCertificadosS (
    n_idSolicitudCertificado INT AUTO_INCREMENT PRIMARY KEY,
    n_idEstudiante INT NOT NULL,
    dt_fechaSolicitud DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    c_estado ENUM('PENDIENTE', 'PROCESANDO', 'REALIZADO', 'ERROR', 'ESPERANDO_DOCUMENTOS') NOT NULL DEFAULT 'PENDIENTE',
    dt_fechaInicioProcesamiento DATETIME NULL,
    dt_fechaFinalizacion DATETIME NULL,
    n_idTipoCertificado INT NOT NULL,
    n_idUsuarioEncargado INT NOT NULL,

    CONSTRAINT fk_TiposCertificados_SolicitudesCertificados
        FOREIGN KEY (n_idTipoCertificado) REFERENCES CertificadosCursosAcademicosUPB_TiposCertificadosS(n_idTipoCertificado)
        ON DELETE RESTRICT,

    CONSTRAINT fk_Usuarios_SolicitudesCertificados
        FOREIGN KEY (n_idUsuarioEncargado) REFERENCES CertificadosCursosAcademicosUPB_UsuariosS(n_idUsuario)
        ON DELETE RESTRICT
);

CREATE TABLE CertificadosCursosAcademicosUPB_DetallesSolicitudesCertificadosS (
    n_idSolicitudCertificado INT NOT NULL,
    n_idAsignatura INT NOT NULL,
    PRIMARY KEY (n_idSolicitudCertificado, n_idAsignatura),

    CONSTRAINT fk_Solicitudes_DetallesSolicitudes
        FOREIGN KEY (n_idSolicitudCertificado) REFERENCES CertificadosCursosAcademicosUPB_SolicitudesCertificadosS(n_idSolicitudCertificado)
        ON DELETE RESTRICT,

    CONSTRAINT fk_Asignaturas_DetallesSolicitudes
        FOREIGN KEY (n_idAsignatura) REFERENCES CertificadosCursosAcademicosUPB_AsignaturasS(n_idAsignatura)
        ON DELETE RESTRICT
);

CREATE TABLE CertificadosCursosAcademicosUPB_CertificadosGeneradosS (
    n_idCertificadoGenerado INT AUTO_INCREMENT PRIMARY KEY,
    t_nombreArchivo VARCHAR(255) NOT NULL,
    bi_archivo LONGBLOB NOT NULL,
    dt_fechaGeneracion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    n_idSolicitudCertificado INT UNIQUE NOT NULL,
    n_idVersionPlantilla INT NOT NULL,

    CONSTRAINT fk_SolicitudesCertificados_CertificadosGenerados
        FOREIGN KEY (n_idSolicitudCertificado) REFERENCES CertificadosCursosAcademicosUPB_SolicitudesCertificadosS(n_idSolicitudCertificado)
        ON DELETE RESTRICT,
        
    CONSTRAINT fk_VersionesPlantillas_CertificadosGenerados
        FOREIGN KEY (n_idVersionPlantilla) REFERENCES CertificadosCursosAcademicosUPB_VersionesPlantillasS(n_idVersionPlantilla)
        ON DELETE RESTRICT
);


-- ========================================================================================================================
--            5. MÓDULO DE AUDITORIA
-- ========================================================================================================================
CREATE TABLE CertificadosCursosAcademicosUPB_LogS (   #tabla que registra al usuario, ip y las operaciones que realiza en la aplicacion
    n_idLog INT AUTO_INCREMENT PRIMARY KEY,
    t_ip VARCHAR(45) NOT NULL,
    t_nombreTabla VARCHAR(150) NOT NULL,
    t_nombreProceso VARCHAR(150) NOT NULL,
    dt_fechaInicio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dt_fechaFin DATETIME NULL,
    n_idUsuario INT NOT NULL,

    CONSTRAINT fk_Usuarios_Log
        FOREIGN KEY (n_idUsuario) REFERENCES CertificadosCursosAcademicosUPB_UsuariosS(n_idUsuario)
        ON DELETE RESTRICT
);



-- ========================================================================================================================
--            TRABAJO PARA CORREGIR EL BACKEND Y PROBAR
-- ========================================================================================================================
INSERT INTO CertificadosCursosAcademicosUPB_TiposDocumentosAcademicosS
(c_codigo, t_nombre)
VALUES
('SYLLABUS', 'Syllabus'),
('CARTA_DESCRIPTIVA', 'Carta Descriptiva');

SELECT
    n_idTipoDocumentoAcademico,
    c_codigo,
    t_nombre
FROM certificadoscursosacademicosupb_tiposdocumentosacademicoss;

SELECT
    n_idVersionDocumento,
    n_idDocumentoAcademico,
    c_periodo,
    t_nombreArchivo,
    OCTET_LENGTH(bi_archivo) AS bytes_archivo,
    dt_fechaCarga
FROM CertificadosCursosAcademicosUPB_VersionesDocumentosS;