package com.certificados.app.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "CertificadosCursosAcademicosUPB_VersionesPlantillasS",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_Plantilla_Version",
                        columnNames = {
                                "n_idPlantillaCertificado",
                                "c_versionFormato"
                        }
                )
        }
)
public class VersionPlantilla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idVersionPlantilla")
    private Integer id;

    @Column(
            name = "c_versionFormato",
            nullable = false,
            length = 20
    )
    private String versionFormato;

    @Column(
            name = "dt_fechaCarga",
            nullable = false
    )
    private LocalDateTime fechaCarga;

    @Column(
            name = "n_idPlantillaCertificado",
            nullable = false
    )
    private Integer idPlantillaCertificado;

    @PrePersist
    protected void prePersist() {
        if (fechaCarga == null) {
            fechaCarga = LocalDateTime.now();
        }
    }

    public VersionPlantilla() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVersionFormato() {
        return versionFormato;
    }

    public void setVersionFormato(String versionFormato) {
        this.versionFormato = versionFormato;
    }

    public LocalDateTime getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(LocalDateTime fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public Integer getIdPlantillaCertificado() {
        return idPlantillaCertificado;
    }

    public void setIdPlantillaCertificado(Integer idPlantillaCertificado) {
        this.idPlantillaCertificado = idPlantillaCertificado;
    }
}
