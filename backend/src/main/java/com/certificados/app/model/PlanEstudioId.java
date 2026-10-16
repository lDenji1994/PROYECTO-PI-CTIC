package com.certificados.app.model;

import java.io.Serializable;
import java.util.Objects;

/** Llave compuesta de PlanEstudio (programa + asignatura). NO MODIFICAR. */
public class PlanEstudioId implements Serializable {

    private Integer idPrograma;
    private Integer idAsignatura;

    public PlanEstudioId() {
    }

    public PlanEstudioId(Integer idPrograma, Integer idAsignatura) {
        this.idPrograma = idPrograma;
        this.idAsignatura = idAsignatura;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanEstudioId)) return false;
        PlanEstudioId that = (PlanEstudioId) o;
        return Objects.equals(idPrograma, that.idPrograma)
                && Objects.equals(idAsignatura, that.idAsignatura);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPrograma, idAsignatura);
    }
}
