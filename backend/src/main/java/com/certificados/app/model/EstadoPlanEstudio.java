package com.certificados.app.model;

/**
 * Estado de un plan de estudios.
 *
 *   VIGENTE:        el plan que se aplica hoy.
 *   ANTIGUO:        un plan anterior (por ejemplo, estudiantes de cohortes viejas).
 *   DESACTUALIZADO: informacion que hay que revisar o reemplazar.
 *
 * NO MODIFICAR los nombres: son los valores de la columna c_estado de la
 * tabla planes (database/07_migracion_planes_estudio.sql).
 */
public enum EstadoPlanEstudio {
    VIGENTE,
    ANTIGUO,
    DESACTUALIZADO
}
