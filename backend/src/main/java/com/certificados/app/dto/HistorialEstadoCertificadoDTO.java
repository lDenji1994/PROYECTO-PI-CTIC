package com.certificados.app.dto;

import com.certificados.app.model.EstadoCertificado;

import java.time.LocalDateTime;

public class HistorialEstadoCertificadoDTO {

    private Long id;
    private Long certificadoId;
    private EstadoCertificado estadoAnterior;
    private EstadoCertificado estadoNuevo;
    private LocalDateTime fechaCambio;

    public HistorialEstadoCertificadoDTO() {
    }

    public HistorialEstadoCertificadoDTO(Long id, Long certificadoId, EstadoCertificado estadoAnterior, EstadoCertificado estadoNuevo, LocalDateTime fechaCambio) {
        this.id = id;
        this.certificadoId = certificadoId;
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

    public Long getCertificadoId() {
        return certificadoId;
    }

    public void setCertificadoId(Long certificadoId) {
        this.certificadoId = certificadoId;
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