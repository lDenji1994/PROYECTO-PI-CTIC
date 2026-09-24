package com.certificados.app.model;

import java.io.Serializable;
import java.util.Objects;

public class DetalleSolicitudCertificadoId implements Serializable {

    private Integer idSolicitudCertificado;
    private Integer idAsignatura;

    public DetalleSolicitudCertificadoId() {
    }

    public DetalleSolicitudCertificadoId(
            Integer idSolicitudCertificado,
            Integer idAsignatura) {

        this.idSolicitudCertificado = idSolicitudCertificado;
        this.idAsignatura = idAsignatura;
    }

    public Integer getIdSolicitudCertificado() {
        return idSolicitudCertificado;
    }

    public void setIdSolicitudCertificado(Integer idSolicitudCertificado) {
        this.idSolicitudCertificado = idSolicitudCertificado;
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

        if (!(o instanceof DetalleSolicitudCertificadoId)) {
            return false;
        }

        DetalleSolicitudCertificadoId that =
                (DetalleSolicitudCertificadoId) o;

        return Objects.equals(
                    idSolicitudCertificado,
                    that.idSolicitudCertificado
                )
                &&
                Objects.equals(
                    idAsignatura,
                    that.idAsignatura
                );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                idSolicitudCertificado,
                idAsignatura
        );
    }
}