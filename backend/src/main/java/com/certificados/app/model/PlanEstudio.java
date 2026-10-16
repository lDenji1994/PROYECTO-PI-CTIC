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
}
