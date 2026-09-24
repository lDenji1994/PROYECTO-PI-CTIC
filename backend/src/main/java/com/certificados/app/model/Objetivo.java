package com.certificados.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "CertificadosCursosAcademicosUPB_ObjetivosS")
public class Objetivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idObjetivo")
    private Integer id;

    @Column(name = "t_descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "n_idVersionDocumento", nullable = false)
    private Integer idVersionDocumento;

    public Objetivo() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getIdVersionDocumento() {
        return idVersionDocumento;
    }

    public void setIdVersionDocumento(Integer idVersionDocumento) {
        this.idVersionDocumento = idVersionDocumento;
    }
}