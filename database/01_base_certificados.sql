CREATE DATABASE CertificadosCursosAcademicosUPB
CHARACTER SET utf8mb4
COLLATE UTF8MB4_UNICODE_CI;

USE CertificadosCursosAcademicosUPB;

CREATE TABLE asignaturas (
    n_idAsignatura INT AUTO_INCREMENT PRIMARY KEY,
    c_codigo VARCHAR(20) UNIQUE NOT NULL,
    t_nombre VARCHAR(150) NOT NULL
);

CREATE TABLE tipos_documento (
    n_idTipoDocumentoAcademico INT AUTO_INCREMENT PRIMARY KEY,
    c_codigo VARCHAR(50) UNIQUE NOT NULL,
    t_nombre VARCHAR(150) UNIQUE NOT NULL
);

CREATE TABLE documentos (
    n_idDocumentoAcademico INT AUTO_INCREMENT PRIMARY KEY,
    c_codigoFormato VARCHAR(50) NULL,
    c_versionFormato VARCHAR(20) NULL,
    n_idAsignatura INT NOT NULL,
    n_idTipoDocumentoAcademico INT NOT NULL,

    CONSTRAINT fk_Asignaturas_DocumentosAcademicos
        FOREIGN KEY (n_idAsignatura) REFERENCES asignaturas(n_idAsignatura)
        ON DELETE RESTRICT,

    CONSTRAINT fk_TiposDocumentosAcademicos_DocumentosAcademicos
        FOREIGN KEY (n_idTipoDocumentoAcademico) REFERENCES tipos_documento(n_idTipoDocumentoAcademico)
        ON DELETE RESTRICT,

    INDEX idx_DocumentosAcademicos_Asignatura (n_idAsignatura)
);

CREATE TABLE programas (
    n_idPrograma INT AUTO_INCREMENT PRIMARY KEY,
    c_codigo VARCHAR(50) UNIQUE NULL,
    t_nombre VARCHAR(200) NOT NULL
);

CREATE TABLE versiones_documento (
    n_idVersionDocumento INT AUTO_INCREMENT PRIMARY KEY,
    n_idDocumentoAcademico INT NOT NULL,
    c_periodo VARCHAR(20) NULL,

    t_nombreArchivo VARCHAR(255) NOT NULL,
    bi_archivo LONGBLOB NOT NULL,

    t_escuela VARCHAR(150) NULL,
    t_facultad VARCHAR(150) NULL,

    c_clasificacionCINE VARCHAR(100) NULL,
    t_nucleoBasicoConocimiento TEXT NULL,

    c_ciclo VARCHAR(100) NULL,
    c_nivelFormacion VARCHAR(100) NULL,

    n_horasTeoricas DECIMAL(6,2) NULL,
    n_horasPracticas DECIMAL(6,2) NULL,
    n_horasLaboratorio DECIMAL(6,2) NULL,
    n_horasIndependientes DECIMAL(6,2) NULL,

    n_creditos DECIMAL(5,2) NULL,

    t_requisitos TEXT NULL,
    t_justificacion TEXT NULL,
    t_descripcion TEXT NULL,
    t_proposito TEXT NULL,

    c_modoCalificacion VARCHAR(100) NULL,
    t_modalidades TEXT NULL,

    t_actaAprobacion TEXT NULL,
    t_actaModificacion TEXT NULL,
    t_observaciones TEXT NULL,

    n_idProgramaDisena INT NULL,

    t_resumen TEXT NULL,

    dt_fechaCarga DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_DocumentosAcademicos_VersionesDocumentos
        FOREIGN KEY (n_idDocumentoAcademico) REFERENCES documentos(n_idDocumentoAcademico)
        ON DELETE RESTRICT,

    CONSTRAINT fk_VersionesDocumentos_ProgramaDisena
        FOREIGN KEY (n_idProgramaDisena) REFERENCES programas(n_idPrograma)
        ON DELETE RESTRICT
);

CREATE TABLE contenidos (
    n_idContenido INT AUTO_INCREMENT PRIMARY KEY,
    n_semana INT NULL,
    t_unidad VARCHAR(150) NULL,
    t_tema VARCHAR(250) NULL,
    t_descripcion TEXT NOT NULL,
    n_orden INT NULL,
    n_idVersionDocumento INT NOT NULL,

    CONSTRAINT fk_VersionesDocumentos_Contenidos
        FOREIGN KEY (n_idVersionDocumento) REFERENCES versiones_documento(n_idVersionDocumento)
        ON DELETE RESTRICT,

    CONSTRAINT chk_Contenidos_Semana
        CHECK (n_semana IS NULL OR n_semana > 0),

    CONSTRAINT chk_Contenidos_Orden
        CHECK (n_orden IS NULL OR n_orden > 0)
);

CREATE TABLE roles (
    n_idRol INT AUTO_INCREMENT PRIMARY KEY,
    c_nombre VARCHAR(50) UNIQUE NOT NULL,
    t_descripcion VARCHAR(150) NULL
);

CREATE TABLE usuarios (
    n_idUsuario INT AUTO_INCREMENT PRIMARY KEY,
    t_nombreCompleto VARCHAR(150) NOT NULL,
    t_usuario VARCHAR(100) UNIQUE NOT NULL,
    t_correo VARCHAR(150) UNIQUE NULL,
    t_contrasena VARCHAR(255) NOT NULL,
    b_activo BOOLEAN NOT NULL DEFAULT TRUE,
    n_idRol INT NOT NULL,

    CONSTRAINT fk_Roles_Usuarios
        FOREIGN KEY (n_idRol) REFERENCES roles(n_idRol)
        ON DELETE RESTRICT
);

INSERT INTO roles (c_nombre, t_descripcion)
VALUES
    ('ADMINISTRADOR',
     'Administrador de la aplicación perteneciente a CTIC UPB'),
    ('AUXILIAR',
     'Usuario encargado de gestionar solicitudes y certificados');

CREATE TABLE tipos_certificado (
    n_idTipoCertificado INT AUTO_INCREMENT PRIMARY KEY,
    t_nombre VARCHAR(150) UNIQUE NOT NULL
);

CREATE TABLE plantillas (
    n_idPlantillaCertificado INT AUTO_INCREMENT PRIMARY KEY,
    t_nombre VARCHAR(150) UNIQUE NOT NULL,
    n_idTipoCertificado INT NOT NULL,

    CONSTRAINT fk_TiposCertificados_PlantillasCertificados
        FOREIGN KEY (n_idTipoCertificado) REFERENCES tipos_certificado(n_idTipoCertificado)
        ON DELETE RESTRICT
);

CREATE TABLE versiones_plantilla (
    n_idVersionPlantilla INT AUTO_INCREMENT PRIMARY KEY,
    c_versionFormato VARCHAR(20) NOT NULL,
    dt_fechaCarga DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    n_idPlantillaCertificado INT NOT NULL,

    CONSTRAINT fk_PlantillasCertificados_VersionesPlantillas
        FOREIGN KEY (n_idPlantillaCertificado) REFERENCES plantillas(n_idPlantillaCertificado)
        ON DELETE RESTRICT,

    CONSTRAINT uq_Plantilla_Version
        UNIQUE (n_idPlantillaCertificado, c_versionFormato)
);

CREATE TABLE campos_plantilla (
    n_idCampoPlantilla INT AUTO_INCREMENT PRIMARY KEY,
    c_codigoCampo VARCHAR(100) NOT NULL,
    t_nombreCampo VARCHAR(150) NOT NULL,
    t_tipoDato ENUM('TEXTO', 'NUMERO', 'FECHA', 'BOOLEANO') NOT NULL,
    c_fuente VARCHAR(50) NOT NULL,
    c_metodoObtencion ENUM('EXTRAER', 'CALCULAR', 'TRANSFORMAR') NOT NULL,
    b_repetible BOOLEAN NOT NULL DEFAULT FALSE,
    t_reglaObtencion TEXT NULL,
    n_idVersionPlantilla INT NOT NULL,

    CONSTRAINT fk_VersionesPlantillas_CamposPlantilla
        FOREIGN KEY (n_idVersionPlantilla) REFERENCES versiones_plantilla(n_idVersionPlantilla)
        ON DELETE RESTRICT,

    CONSTRAINT uq_VersionPlantilla_CodigoCampo
        UNIQUE (n_idVersionPlantilla, c_codigoCampo)
);

CREATE TABLE elementos_plantilla (
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
    n_idCampoPlantilla INT NULL,

    CONSTRAINT fk_VersionesPlantillas_ElementosPlantilla
        FOREIGN KEY (n_idVersionPlantilla) REFERENCES versiones_plantilla(n_idVersionPlantilla)
        ON DELETE RESTRICT,

    CONSTRAINT fk_CamposPlantilla_ElementosPlantilla
        FOREIGN KEY (n_idCampoPlantilla) REFERENCES campos_plantilla(n_idCampoPlantilla)
        ON DELETE RESTRICT,

    CONSTRAINT chk_ElementosPlantilla_Orden
    CHECK (n_orden IS NULL OR n_orden > 0),

    CONSTRAINT chk_ElementosPlantilla_Campo
    CHECK (c_tipoElemento <> 'CAMPO' OR n_idCampoPlantilla IS NOT NULL)
);

CREATE TABLE planes (
    n_idPlan INT AUTO_INCREMENT PRIMARY KEY,
    n_idPrograma INT NOT NULL,
    t_nombre VARCHAR(100) NOT NULL,
    c_estado ENUM('VIGENTE', 'ANTIGUO', 'DESACTUALIZADO') NOT NULL DEFAULT 'VIGENTE',

    CONSTRAINT uq_Planes_programa_nombre
        UNIQUE (n_idPrograma, t_nombre),

    CONSTRAINT fk_Programas_Planes
        FOREIGN KEY (n_idPrograma) REFERENCES programas(n_idPrograma)
        ON DELETE CASCADE
);

CREATE TABLE planes_estudio (
    n_idPlan INT NOT NULL,
    n_idAsignatura INT NOT NULL,
    n_semestre INT NOT NULL,
    PRIMARY KEY (n_idPlan, n_idAsignatura),

    CONSTRAINT fk_Planes_PlanesEstudio
        FOREIGN KEY (n_idPlan) REFERENCES planes(n_idPlan)
        ON DELETE CASCADE,

    CONSTRAINT fk_Asignaturas_PlanesEstudio
        FOREIGN KEY (n_idAsignatura) REFERENCES asignaturas(n_idAsignatura)
        ON DELETE CASCADE,

    CONSTRAINT chk_PlanesEstudio_semestre
        CHECK (n_semestre BETWEEN 1 AND 20)
);

CREATE TABLE solicitudes (
    n_idSolicitudCertificado INT AUTO_INCREMENT PRIMARY KEY,
    n_idEstudiante INT NOT NULL,
    dt_fechaSolicitud DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    c_estado ENUM('PENDIENTE', 'PROCESANDO', 'REALIZADO', 'ENTREGADO', 'ERROR', 'ESPERANDO_DOCUMENTOS') NOT NULL DEFAULT 'PENDIENTE',
    dt_fechaInicioProcesamiento DATETIME NULL,
    dt_fechaFinalizacion DATETIME NULL,
    n_idTipoCertificado INT NOT NULL,
    n_idUsuarioEncargado INT NOT NULL,
    t_nombreEstudiante VARCHAR(150) NULL,
    dt_fechaEntrega DATETIME NULL,
    c_tipoContenido ENUM('RESUMIDO', 'COMPLETO') NOT NULL DEFAULT 'RESUMIDO',
    c_alcance ENUM('ASIGNATURAS', 'SEMESTRES', 'PROGRAMA') NOT NULL DEFAULT 'ASIGNATURAS',
    n_idPrograma INT NULL,
    n_idPlan INT NULL,
    t_semestres VARCHAR(100) NULL,

    CONSTRAINT fk_Programas_SolicitudesCertificados
        FOREIGN KEY (n_idPrograma) REFERENCES programas(n_idPrograma)
        ON DELETE SET NULL,

    CONSTRAINT fk_Planes_Solicitudes
        FOREIGN KEY (n_idPlan) REFERENCES planes(n_idPlan)
        ON DELETE SET NULL,

    CONSTRAINT fk_TiposCertificados_SolicitudesCertificados
        FOREIGN KEY (n_idTipoCertificado) REFERENCES tipos_certificado(n_idTipoCertificado)
        ON DELETE RESTRICT,

    CONSTRAINT fk_Usuarios_SolicitudesCertificados
        FOREIGN KEY (n_idUsuarioEncargado) REFERENCES usuarios(n_idUsuario)
        ON DELETE RESTRICT
);

CREATE TABLE solicitud_asignaturas (
    n_idSolicitudCertificado INT NOT NULL,
    n_idAsignatura INT NOT NULL,
    n_idVersionDocumento INT NULL,
    PRIMARY KEY (n_idSolicitudCertificado, n_idAsignatura),

    CONSTRAINT fk_Solicitudes_DetallesSolicitudes
        FOREIGN KEY (n_idSolicitudCertificado) REFERENCES solicitudes(n_idSolicitudCertificado)
        ON DELETE RESTRICT,

    CONSTRAINT fk_Asignaturas_DetallesSolicitudes
        FOREIGN KEY (n_idAsignatura) REFERENCES asignaturas(n_idAsignatura)
        ON DELETE RESTRICT,

    CONSTRAINT fk_VersionesDocumentos_DetallesSolicitudes
        FOREIGN KEY (n_idVersionDocumento) REFERENCES versiones_documento(n_idVersionDocumento)
        ON DELETE SET NULL
);

CREATE TABLE certificados_generados (
    n_idCertificadoGenerado INT AUTO_INCREMENT PRIMARY KEY,
    t_nombreArchivo VARCHAR(255) NOT NULL,
    bi_archivo LONGBLOB NOT NULL,
    dt_fechaGeneracion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    n_idSolicitudCertificado INT UNIQUE NOT NULL,
    n_idVersionPlantilla INT NOT NULL,

    CONSTRAINT fk_SolicitudesCertificados_CertificadosGenerados
        FOREIGN KEY (n_idSolicitudCertificado) REFERENCES solicitudes(n_idSolicitudCertificado)
        ON DELETE RESTRICT,

    CONSTRAINT fk_VersionesPlantillas_CertificadosGenerados
        FOREIGN KEY (n_idVersionPlantilla) REFERENCES versiones_plantilla(n_idVersionPlantilla)
        ON DELETE RESTRICT
);

CREATE TABLE bitacora (
    n_idLog INT AUTO_INCREMENT PRIMARY KEY,
    t_ip VARCHAR(45) NOT NULL,
    t_nombreTabla VARCHAR(150) NOT NULL,
    t_nombreProceso VARCHAR(150) NOT NULL,
    dt_fechaInicio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dt_fechaFin DATETIME NULL,
    n_idUsuario INT NOT NULL,

    CONSTRAINT fk_Usuarios_Log
        FOREIGN KEY (n_idUsuario) REFERENCES usuarios(n_idUsuario)
        ON DELETE RESTRICT
);
