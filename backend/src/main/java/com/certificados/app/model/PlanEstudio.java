package com.certificados.app.model;

import jakarta.persistence.*;

/**
 * ASIGNATURA DENTRO DE UN PLAN DE ESTUDIOS: en que semestre va.
 * Tabla planes_estudio (migraciones 05 y 07).
 *
 * Con esto una solicitud puede pedir uno o varios semestres de un plan,
 * o el plan completo, sin marcar las asignaturas una por una.
 * Una asignatura puede estar en varios planes (con semestres distintos),
 * pero solo una vez en cada plan.
 *
 * NO MODIFICAR los nombres de columnas (deben coincidir con la base de datos).
 */
@Entity
@Table(name = "planes_estudio")
@IdClass(PlanEstudioId.class)
public class PlanEstudio {

    @Id
    @Column(name = "n_idPlan", nullable = false)
    private Integer idPlan;

    @Id
    @Column(name = "n_idAsignatura", nullable = false)
    private Integer idAsignatura;

    @Column(name = "n_semestre", nullable = false)
    private Integer semestre;

    public PlanEstudio() {
    }

    public PlanEstudio(Integer idPlan, Integer idAsignatura, Integer semestre) {
        this.idPlan = idPlan;
        this.idAsignatura = idAsignatura;
        this.semestre = semestre;
    }

    public Integer getIdPlan() {
        return idPlan;
    }

    public void setIdPlan(Integer idPlan) {
        this.idPlan = idPlan;
    }

    public Integer getIdAsignatura() {
        return idAsignatura;
    }

    public void setIdAsignatura(Integer idAsignatura) {
        this.idAsignatura = idAsignatura;
    }

    public Integer getSemestre() {
        return semestre;
    }

    public void setSemestre(Integer semestre) {
        this.semestre = semestre;
    }
}
