package com.certificados.app.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "CertificadosCursosAcademicosUPB_SolicitudesCertificadosS")
public class SolicitudCertificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idSolicitudCertificado")
    private Integer id;

    /**
     * Identificador del estudiante ingresado por la auxiliar.
     * No existe FK porque el estudiante no es una entidad persistida
     * dentro de este Track.
     */
    @Column(name = "n_idEstudiante", nullable = false)
    private Integer idEstudiante;

    @Column(
            name = "dt_fechaSolicitud",
            nullable = false
    )
    private LocalDateTime fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "c_estado",
            nullable = false,
            length = 30
    )
    private EstadoSolicitudCertificado estado = EstadoSolicitudCertificado.PENDIENTE;

    @Column(name = "dt_fechaInicioProcesamiento")
    private LocalDateTime fechaInicioProcesamiento;

    @Column(name = "dt_fechaFinalizacion")
    private LocalDateTime fechaFinalizacion;

    /**
     * ID del tipo de certificado seleccionado.
     * Se mantiene como identificador para evitar acoplar todavía
     * esta entidad al modelo nuevo de tipos y plantillas.
     */
    @Column(name = "n_idTipoCertificado", nullable = false)
    private Integer idTipoCertificado;

    /**
     * Usuario encargado de procesar la solicitud.
     */
    @Column(name = "n_idUsuarioEncargado", nullable = false)
    private Integer idUsuarioEncargado;

    @PrePersist
    protected void prePersist() {
        if (fechaSolicitud == null) {
            fechaSolicitud = LocalDateTime.now();
        }

        if (estado == null) {
            estado = EstadoSolicitudCertificado.PENDIENTE;
        }
    }

    public SolicitudCertificado() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(Integer idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public EstadoSolicitudCertificado getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitudCertificado estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaInicioProcesamiento() {
        return fechaInicioProcesamiento;
    }

    public void setFechaInicioProcesamiento(LocalDateTime fechaInicioProcesamiento) {
        this.fechaInicioProcesamiento = fechaInicioProcesamiento;
    }

    public LocalDateTime getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(LocalDateTime fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public Integer getIdTipoCertificado() {
        return idTipoCertificado;
    }

    public void setIdTipoCertificado(Integer idTipoCertificado) {
        this.idTipoCertificado = idTipoCertificado;
    }

    public Integer getIdUsuarioEncargado() {
        return idUsuarioEncargado;
    }

    public void setIdUsuarioEncargado(Integer idUsuarioEncargado) {
        this.idUsuarioEncargado = idUsuarioEncargado;
    }
}
