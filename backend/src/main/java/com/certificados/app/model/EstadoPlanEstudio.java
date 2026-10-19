package com.certificados.app.model;

/**
 * Estado de una asignatura dentro del plan de estudios de un programa.
 *
 *   VIGENTE:        el plan que se aplica hoy.
 *   ANTIGUO:        un plan anterior (por ejemplo, estudiantes de cohortes viejas).
 *   DESACTUALIZADO: informacion que hay que revisar o reemplazar.
 *
 * NO MODIFICAR los nombres: son los valores de la columna c_estadoPlan
 * (database/07_migracion_plan_estudio_estado.sql).
 */
public enum EstadoPlanEstudio {
    VIGENTE,
    ANTIGUO,
    DESACTUALIZADO
}
