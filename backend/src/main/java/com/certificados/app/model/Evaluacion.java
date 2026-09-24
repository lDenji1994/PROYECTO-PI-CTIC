package com.certificados.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "CertificadosCursosAcademicosUPB_EvaluacionesS")
public class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idEvaluacion")
    private Integer id;

    @Column(name = "t_tipo", length = 100)
    private String tipo;

    @Column(name = "t_descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "n_porcentaje", precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(name = "n_orden")
    private Integer orden;

    @Column(name = "n_idVersionDocumento", nullable = false)
    private Integer idVersionDocumento;

    public Evaluacion() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
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