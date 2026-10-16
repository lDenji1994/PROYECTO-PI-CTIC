package com.certificados.app.dto;

import com.certificados.app.model.EstadoSolicitudCertificado;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

import com.certificados.app.model.AlcanceSolicitud;
import com.certificados.app.model.TipoContenidoCertificado;

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

    /* ---- Agregados en la migracion 05 ---- */
    /** Nombre del estudiante (obligatorio al crear la solicitud). */
    private String nombreEstudiante;
    /** Cuando se le entrego el certificado al estudiante (estado ENTREGADO). */
    private LocalDateTime fechaEntrega;
    /** RESUMIDO (por defecto) o COMPLETO. */
    private TipoContenidoCertificado tipoContenido;
    /** ASIGNATURAS (por defecto), SEMESTRES o PROGRAMA. */
    private AlcanceSolicitud alcance;
    /** Programa (alcance SEMESTRES o PROGRAMA). */
    private Integer idPrograma;
    /** Semestres pedidos (alcance SEMESTRES), ej. [1, 2, 5]. */
    private List<Integer> semestres;

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

    public String getNombreEstudiante() { return nombreEstudiante; }
    public void setNombreEstudiante(String nombreEstudiante) { this.nombreEstudiante = nombreEstudiante; }
    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    public TipoContenidoCertificado getTipoContenido() { return tipoContenido; }
    public void setTipoContenido(TipoContenidoCertificado tipoContenido) { this.tipoContenido = tipoContenido; }
    public AlcanceSolicitud getAlcance() { return alcance; }
    public void setAlcance(AlcanceSolicitud alcance) { this.alcance = alcance; }
    public Integer getIdPrograma() { return idPrograma; }
    public void setIdPrograma(Integer idPrograma) { this.idPrograma = idPrograma; }
    public List<Integer> getSemestres() { return semestres; }
    public void setSemestres(List<Integer> semestres) { this.semestres = semestres; }
}
