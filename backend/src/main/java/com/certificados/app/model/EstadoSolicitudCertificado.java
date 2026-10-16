package com.certificados.app.model;

/**
 * Estados de una solicitud de certificado.
 *
 * Flujo normal:  PENDIENTE -> PROCESANDO -> REALIZADO -> ENTREGADO
 *   - REALIZADO: el PDF ya se genero, pero todavia no se le entrega al estudiante.
 *   - ENTREGADO: el certificado ya se le entrego al estudiante (migracion 05).
 *
 * NO MODIFICAR los nombres: son los valores de la columna c_estado (ENUM en MySQL).
 * Si se agrega un estado hay que agregarlo tambien en la base de datos, en
 * SolicitudCertificadoService.validarTransicion y en app.js (ESTADOS_SOLICITUD).
 */
public enum EstadoSolicitudCertificado {
    PENDIENTE,
    PROCESANDO,
    ESPERANDO_DOCUMENTOS,
    REALIZADO,
    ENTREGADO,
    ERROR
}
