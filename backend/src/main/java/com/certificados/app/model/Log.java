package com.certificados.app.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "CertificadosCursosAcademicosUPB_LogS")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idLog")
    private Integer id;

    @Column(
            name = "t_ip",
            nullable = false,
            length = 45
    )
    private String ip;

    @Column(
            name = "t_nombreTabla",
            nullable = false,
            length = 150
    )
    private String nombreTabla;

    @Column(
            name = "t_nombreProceso",
            nullable = false,
            length = 150
    )
    private String nombreProceso;

    @Column(
            name = "dt_fechaInicio",
            nullable = false
    )
    private LocalDateTime fechaInicio;

    @Column(name = "dt_fechaFin")
    private LocalDateTime fechaFin;

    @Column(
            name = "n_idUsuario",
            nullable = false
    )
    private Integer idUsuario;

    @PrePersist
    protected void prePersist() {
        if (fechaInicio == null) {
            fechaInicio = LocalDateTime.now();
        }
    }

    public Log() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getNombreTabla() {
        return nombreTabla;
    }

    public void setNombreTabla(String nombreTabla) {
        this.nombreTabla = nombreTabla;
    }

    public String getNombreProceso() {
        return nombreProceso;
    }

    public void setNombreProceso(String nombreProceso) {
        this.nombreProceso = nombreProceso;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
}