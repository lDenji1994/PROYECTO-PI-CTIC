package com.certificados.app.model;

/**
 * Que abarca una solicitud de certificado.
 *
 *   ASIGNATURAS: una o varias asignaturas escogidas a mano (o con el Kardex).
 *   SEMESTRES:   uno o varios semestres de un programa (todos o solo algunos).
 *   PROGRAMA:    el programa academico completo (todos sus semestres).
 *
 * En SEMESTRES y PROGRAMA las asignaturas salen del plan de estudios del
 * programa (tabla PlanesEstudioS) y el certificado las agrupa por semestre.
 *
 * NO MODIFICAR los nombres: son los valores de la columna c_alcance.
 */
public enum AlcanceSolicitud {
    ASIGNATURAS,
    SEMESTRES,
    PROGRAMA
}
