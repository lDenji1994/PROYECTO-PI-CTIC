package com.certificados.app.model;

import java.io.Serializable;
import java.util.Objects;

/** Llave compuesta de PlanEstudio (plan + asignatura). NO MODIFICAR. */
public class PlanEstudioId implements Serializable {

    private Integer idPlan;
    private Integer idAsignatura;

    public PlanEstudioId() {
    }

    public PlanEstudioId(Integer idPlan, Integer idAsignatura) {
        this.idPlan = idPlan;
        this.idAsignatura = idAsignatura;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanEstudioId)) return false;
        PlanEstudioId that = (PlanEstudioId) o;
        return Objects.equals(idPlan, that.idPlan)
                && Objects.equals(idAsignatura, that.idAsignatura);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPlan, idAsignatura);
    }
}
