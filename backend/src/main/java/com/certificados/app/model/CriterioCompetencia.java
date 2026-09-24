package com.certificados.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "CertificadosCursosAcademicosUPB_CriteriosCompetencias")
public class CriterioCompetencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idCriterioCompetencia")
    private Integer id;

    @Column(name = "t_descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "n_idCompetencia", nullable = false)
    private Integer idCompetencia;

    public CriterioCompetencia() {
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

    public Integer getIdCompetencia() {
        return idCompetencia;
    }

    public void setIdCompetencia(Integer idCompetencia) {
        this.idCompetencia = idCompetencia;
    }
}