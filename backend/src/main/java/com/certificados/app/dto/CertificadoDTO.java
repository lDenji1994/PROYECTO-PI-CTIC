package com.certificados.app.dto;

import com.certificados.app.model.EstadoCertificado;
import com.certificados.app.model.TipoCertificado;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class CertificadoDTO {

    private Long id;
    private String codigoVerificacion;

    @NotNull(message = "El tipo de certificado es obligatorio")
    private TipoCertificado tipo;

    private EstadoCertificado estado;
    private LocalDate fechaSolicitud;
    private LocalDate fechaEmision;
    private String observaciones;

    @NotNull(message = "El estudiante es obligatorio")
    private Long estudianteId;

    private String nombreEstudiante;

    public CertificadoDTO() {
    }

    public CertificadoDTO(Long id, String codigoVerificacion, TipoCertificado tipo, EstadoCertificado estado, LocalDate fechaSolicitud, LocalDate fechaEmision, String observaciones, Long estudianteId, String nombreEstudiante) {
        this.id = id;
        this.codigoVerificacion = codigoVerificacion;
        this.tipo = tipo;
        this.estado = estado;
        this.fechaSolicitud = fechaSolicitud;
        this.fechaEmision = fechaEmision;
        this.observaciones = observaciones;
        this.estudianteId = estudianteId;
        this.nombreEstudiante = nombreEstudiante;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoVerificacion() {
        return codigoVerificacion;
    }

    public void setCodigoVerificacion(String codigoVerificacion) {
        this.codigoVerificacion = codigoVerificacion;
    }

    public TipoCertificado getTipo() {
        return tipo;
    }

    public void setTipo(TipoCertificado tipo) {
        this.tipo = tipo;
    }

    public EstadoCertificado getEstado() {
        return estado;
    }

    public void setEstado(EstadoCertificado estado) {
        this.estado = estado;
    }

    public LocalDate getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDate fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Long getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Long estudianteId) {
        this.estudianteId = estudianteId;
    }

    public String getNombreEstudiante() {
        return nombreEstudiante;
    }

    public void setNombreEstudiante(String nombreEstudiante) {
        this.nombreEstudiante = nombreEstudiante;
    }
}