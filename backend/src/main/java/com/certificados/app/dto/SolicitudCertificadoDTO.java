package com.certificados.app.dto;

import com.certificados.app.model.EstadoSolicitudCertificado;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class SolicitudCertificadoDTO {

    private Integer id;

    @NotNull(message = "El identificador del estudiante es obligatorio")
    private Integer idEstudiante;

    @NotNull(message = "El tipo de certificado es obligatorio")
    private Integer idTipoCertificado;

    @NotNull(message = "El usuario encargado es obligatorio")
    private Integer idUsuarioEncargado;

    private EstadoSolicitudCertificado estado;

    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaInicioProcesamiento;
    private LocalDateTime fechaFinalizacion;

    public SolicitudCertificadoDTO() {
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

    public EstadoSolicitudCertificado getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitudCertificado estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
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
}
