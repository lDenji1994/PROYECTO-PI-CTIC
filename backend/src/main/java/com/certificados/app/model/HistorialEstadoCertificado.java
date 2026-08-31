package com.certificados.app.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Registro de auditoria: cada fila representa UN cambio de estado
 * de un Certificado (ej. PENDIENTE -> EMITIDO). No se actualiza ni
 * se borra nunca despues de creado, solo se inserta (US-21.05:
 * el historial no debe poder modificarse).
 */
@Entity
@Table(name = "historial_estados_certificado")
public class HistorialEstadoCertificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificado_id", nullable = false)
    private Certificado certificado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCertificado estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCertificado estadoNuevo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCambio = LocalDateTime.now();

    public HistorialEstadoCertificado() {
    }

    public HistorialEstadoCertificado(Long id, Certificado certificado, EstadoCertificado estadoAnterior, EstadoCertificado estadoNuevo, LocalDateTime fechaCambio) {
        this.id = id;
        this.certificado = certificado;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.fechaCambio = fechaCambio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Certificado getCertificado() {
        return certificado;
    }

    public void setCertificado(Certificado certificado) {
        this.certificado = certificado;
    }

    public EstadoCertificado getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(EstadoCertificado estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

    public EstadoCertificado getEstadoNuevo() {
        return estadoNuevo;
    }

    public void setEstadoNuevo(EstadoCertificado estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }

    public LocalDateTime getFechaCambio() {
        return fechaCambio;
    }

    public void setFechaCambio(LocalDateTime fechaCambio) {
        this.fechaCambio = fechaCambio;
    }
}