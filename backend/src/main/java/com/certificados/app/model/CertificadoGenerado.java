package com.certificados.app.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "certificados_generados"
)
public class CertificadoGenerado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idCertificadoGenerado")
    private Integer id;

    @Column(
            name = "t_nombreArchivo",
            nullable = false,
            length = 255
    )
    private String nombreArchivo;

    /**
     * Ruta del PDF DENTRO de la carpeta de archivos (migracion 09), ej.
     * "certificados/2026/8b7d....pdf". El PDF ya no se guarda en la base.
     */
    @Column(
            name = "t_rutaArchivo",
            nullable = false,
            length = 500
    )
    private String rutaArchivo;

    @Column(
            name = "dt_fechaGeneracion",
            nullable = false
    )
    private LocalDateTime fechaGeneracion;

    @Column(
            name = "n_idSolicitudCertificado",
            nullable = false,
            unique = true
    )
    private Integer idSolicitudCertificado;

    @Column(
            name = "n_idVersionPlantilla",
            nullable = false
    )
    private Integer idVersionPlantilla;

    @PrePersist
    protected void prePersist() {
        if (fechaGeneracion == null) {
            fechaGeneracion = LocalDateTime.now();
        }
    }

    public CertificadoGenerado() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public Integer getIdSolicitudCertificado() {
        return idSolicitudCertificado;
    }

    public void setIdSolicitudCertificado(Integer idSolicitudCertificado) {
        this.idSolicitudCertificado = idSolicitudCertificado;
    }

    public Integer getIdVersionPlantilla() {
        return idVersionPlantilla;
    }

    public void setIdVersionPlantilla(Integer idVersionPlantilla) {
        this.idVersionPlantilla = idVersionPlantilla;
    }
}