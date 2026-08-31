package com.certificados.app.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "certificados")
public class Certificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String codigoVerificacion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoCertificado tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCertificado estado = EstadoCertificado.PENDIENTE;

    @Column(nullable = false)
    private LocalDate fechaSolicitud = LocalDate.now();

    private LocalDate fechaEmision;

    @Column(length = 500)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    @JsonBackReference
    private Estudiante estudiante;

    @Column(nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    public Certificado() {
    }

    public Certificado(Long id, String codigoVerificacion, TipoCertificado tipo, EstadoCertificado estado, LocalDate fechaSolicitud, LocalDate fechaEmision, String observaciones, Estudiante estudiante, LocalDateTime creadoEn) {
        this.id = id;
        this.codigoVerificacion = codigoVerificacion;
        this.tipo = tipo;
        this.estado = estado;
        this.fechaSolicitud = fechaSolicitud;
        this.fechaEmision = fechaEmision;
        this.observaciones = observaciones;
        this.estudiante = estudiante;
        this.creadoEn = creadoEn;
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

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Certificado that = (Certificado) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}