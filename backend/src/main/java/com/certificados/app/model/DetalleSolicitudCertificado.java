package com.certificados.app.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "CertificadosCursosAcademicosUPB_DetallesSolicitudesCertificadosS"
)
@IdClass(DetalleSolicitudCertificadoId.class)
public class DetalleSolicitudCertificado {

    @Id
    @Column(name = "n_idSolicitudCertificado")
    private Integer idSolicitudCertificado;

    @Id
    @Column(name = "n_idAsignatura")
    private Integer idAsignatura;

    public DetalleSolicitudCertificado() {
    }

    public DetalleSolicitudCertificado(
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
}