package com.certificados.app.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes")
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

    /* ---------------- Agregados en la migracion 05 ---------------- */

    /** Nombre del estudiante (ademas del ID). Sale en el certificado. */
    @Column(name = "t_nombreEstudiante", length = 150)
    private String nombreEstudiante;

    /** Fecha en que el certificado se le entrego al estudiante (estado ENTREGADO). */
    @Column(name = "dt_fechaEntrega")
    private LocalDateTime fechaEntrega;

    /** RESUMIDO o COMPLETO: que tan detallado sale cada asignatura. */
    @Enumerated(EnumType.STRING)
    @Column(name = "c_tipoContenido", nullable = false, length = 20)
    private TipoContenidoCertificado tipoContenido = TipoContenidoCertificado.RESUMIDO;

    /** ASIGNATURAS, SEMESTRES o PROGRAMA. */
    @Enumerated(EnumType.STRING)
    @Column(name = "c_alcance", nullable = false, length = 20)
    private AlcanceSolicitud alcance = AlcanceSolicitud.ASIGNATURAS;

    /** Programa de la solicitud (solo alcance SEMESTRES o PROGRAMA). */
    @Column(name = "n_idPrograma")
    private Integer idPrograma;

    /** Plan de estudios usado (solo alcance SEMESTRES o PROGRAMA; migracion 07). */
    @Column(name = "n_idPlan")
    private Integer idPlan;

    /** Semestres pedidos separados por coma, ej. "1,2,5" (solo alcance SEMESTRES). */
    @Column(name = "t_semestres", length = 100)
    private String semestres;

    @PrePersist
    protected void prePersist() {
        if (fechaSolicitud == null) {
            fechaSolicitud = LocalDateTime.now();
        }

        if (estado == null) {
            estado = EstadoSolicitudCertificado.PENDIENTE;
        }
        if (tipoContenido == null) {
            tipoContenido = TipoContenidoCertificado.RESUMIDO;
        }
        if (alcance == null) {
            alcance = AlcanceSolicitud.ASIGNATURAS;
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

    public String getNombreEstudiante() {
        return nombreEstudiante;
    }

    public void setNombreEstudiante(String nombreEstudiante) {
        this.nombreEstudiante = nombreEstudiante;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDateTime fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public TipoContenidoCertificado getTipoContenido() {
        return tipoContenido;
    }

    public void setTipoContenido(TipoContenidoCertificado tipoContenido) {
        this.tipoContenido = tipoContenido;
    }

    public AlcanceSolicitud getAlcance() {
        return alcance;
    }

    public void setAlcance(AlcanceSolicitud alcance) {
        this.alcance = alcance;
    }

    public Integer getIdPlan() {
        return idPlan;
    }

    public void setIdPlan(Integer idPlan) {
        this.idPlan = idPlan;
    }

    public Integer getIdPrograma() {
        return idPrograma;
    }

    public void setIdPrograma(Integer idPrograma) {
        this.idPrograma = idPrograma;
    }

    public String getSemestres() {
        return semestres;
    }

    public void setSemestres(String semestres) {
        this.semestres = semestres;
    }
}
