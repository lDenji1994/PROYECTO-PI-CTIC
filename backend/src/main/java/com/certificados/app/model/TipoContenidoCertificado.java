package com.certificados.app.model;

/**
 * Que tan detallado sale el contenido de cada asignatura en el certificado.
 *
 *   RESUMIDO: un parrafo breve por asignatura (el "resumen" de los datos del
 *             curso o, si esta vacio, la descripcion del curso).
 *   COMPLETO: todo el contenido registrado (descripcion, proposito,
 *             justificacion, requisitos, contenido tema por tema y
 *             modalidades). Puede ocupar varias
 *             paginas por asignatura.
 *
 * NO MODIFICAR los nombres: son los valores de la columna c_tipoContenido.
 */
public enum TipoContenidoCertificado {
    RESUMIDO,
    COMPLETO
}
