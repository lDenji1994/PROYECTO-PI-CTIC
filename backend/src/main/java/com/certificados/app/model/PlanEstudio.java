package com.certificados.app.model;

import jakarta.persistence.*;

/**
 * PLAN DE ESTUDIOS: en que semestre de un programa va una asignatura.
 * Tabla PlanesEstudioS (migracion 05).
 *
 * Con esto una solicitud puede pedir uno o varios semestres de un programa,
 * o el programa completo, sin marcar las asignaturas una por una.
 * Una asignatura puede estar en varios programas (con semestres distintos),
 * pero solo una vez en cada programa.
 *
 * Cada fila guarda ademas el nombre o periodo del plan al que pertenece la
 * asignatura (ej. "Plan 2026" o "2020-1") y su estado (vigente, antiguo o
 * desactualizado). Columnas agregadas en la migracion 07.
 *
 * NO MODIFICAR los nombres de columnas (deben coincidir con la base de datos).
 */
@Entity
@Table(name = "planes_estudio")
@IdClass(PlanEstudioId.class)
public class PlanEstudio {

    @Id
    @Column(name = "n_idPrograma", nullable = false)
    private Integer idPrograma;

    @Id
    @Column(name = "n_idAsignatura", nullable = false)
    private Integer idAsignatura;

    @Column(name = "n_semestre", nullable = false)
    private Integer semestre;

    /** Nombre o periodo del plan, ej. "Plan 2026" o "2020-1" (opcional). */
    @Column(name = "t_plan", length = 60)
    private String plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "c_estadoPlan", nullable = false, length = 20)
    private EstadoPlanEstudio estadoPlan = EstadoPlanEstudio.VIGENTE;

    public PlanEstudio() {
    }

    public PlanEstudio(Integer idPrograma, Integer idAsignatura, Integer semestre) {
        this.idPrograma = idPrograma;
        this.idAsignatura = idAsignatura;
        this.semestre = semestre;
    }

    public Integer getIdPrograma() {
        return idPrograma;
    }

    public void setIdPrograma(Integer idPrograma) {
        this.idPrograma = idPrograma;
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

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public EstadoPlanEstudio getEstadoPlan() {
        return estadoPlan;
    }

    public void setEstadoPlan(EstadoPlanEstudio estadoPlan) {
        this.estadoPlan = estadoPlan;
    }
}
