package com.certificados.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "CertificadosCursosAcademicosUPB_ContenidosS")
public class Contenido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idContenido")
    private Integer id;

    @Column(name = "n_semana")
    private Integer semana;

    @Column(name = "t_unidad", length = 150)
    private String unidad;

    @Column(name = "t_tema", length = 250)
    private String tema;

    @Column(name = "t_descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "n_orden")
    private Integer orden;

    @Column(name = "n_idVersionDocumento", nullable = false)
    private Integer idVersionDocumento;

    public Contenido() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSemana() {
        return semana;
    }

    public void setSemana(Integer semana) {
        this.semana = semana;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public Integer getIdVersionDocumento() {
        return idVersionDocumento;
    }

    public void setIdVersionDocumento(Integer idVersionDocumento) {
        this.idVersionDocumento = idVersionDocumento;
    }
}