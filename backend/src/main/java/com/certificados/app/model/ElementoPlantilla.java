package com.certificados.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "CertificadosCursosAcademicosUPB_ElementosPlantillas")
public class ElementoPlantilla {

    public enum TipoElemento {
        TEXTO,
        IMAGEN,
        CAMPO,
        LINEA,
        FIRMA,
        TABLA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idElementoPlantilla")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "c_tipoElemento", nullable = false)
    private TipoElemento tipoElemento;

    @Column(name = "t_contenido", columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "n_posicionX", precision = 8, scale = 2)
    private BigDecimal posicionX;

    @Column(name = "n_posicionY", precision = 8, scale = 2)
    private BigDecimal posicionY;

    @Column(name = "n_ancho", precision = 8, scale = 2)
    private BigDecimal ancho;

    @Column(name = "n_alto", precision = 8, scale = 2)
    private BigDecimal alto;

    @Column(name = "n_tamanoFuente", precision = 5, scale = 2)
    private BigDecimal tamanoFuente;

    @Column(name = "c_tipoFuente", length = 100)
    private String tipoFuente;

    @Column(name = "c_alineacion", length = 20)
    private String alineacion;

    @Column(name = "n_orden")
    private Integer orden;

    @Column(name = "n_idVersionPlantilla", nullable = false)
    private Integer idVersionPlantilla;

    @Column(name = "n_idSeccionPlantilla")
    private Integer idSeccionPlantilla;

    @Column(name = "n_idCampoPlantilla")
    private Integer idCampoPlantilla;

    public ElementoPlantilla() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TipoElemento getTipoElemento() {
        return tipoElemento;
    }

    public void setTipoElemento(TipoElemento tipoElemento) {
        this.tipoElemento = tipoElemento;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public BigDecimal getPosicionX() {
        return posicionX;
    }

    public void setPosicionX(BigDecimal posicionX) {
        this.posicionX = posicionX;
    }

    public BigDecimal getPosicionY() {
        return posicionY;
    }

    public void setPosicionY(BigDecimal posicionY) {
        this.posicionY = posicionY;
    }

    public BigDecimal getAncho() {
        return ancho;
    }

    public void setAncho(BigDecimal ancho) {
        this.ancho = ancho;
    }

    public BigDecimal getAlto() {
        return alto;
    }

    public void setAlto(BigDecimal alto) {
        this.alto = alto;
    }

    public BigDecimal getTamanoFuente() {
        return tamanoFuente;
    }

    public void setTamanoFuente(BigDecimal tamanoFuente) {
        this.tamanoFuente = tamanoFuente;
    }

    public String getTipoFuente() {
        return tipoFuente;
    }

    public void setTipoFuente(String tipoFuente) {
        this.tipoFuente = tipoFuente;
    }

    public String getAlineacion() {
        return alineacion;
    }

    public void setAlineacion(String alineacion) {
        this.alineacion = alineacion;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public Integer getIdVersionPlantilla() {
        return idVersionPlantilla;
    }

    public void setIdVersionPlantilla(Integer idVersionPlantilla) {
        this.idVersionPlantilla = idVersionPlantilla;
    }

    public Integer getIdSeccionPlantilla() {
        return idSeccionPlantilla;
    }

    public void setIdSeccionPlantilla(Integer idSeccionPlantilla) {
        this.idSeccionPlantilla = idSeccionPlantilla;
    }

    public Integer getIdCampoPlantilla() {
        return idCampoPlantilla;
    }

    public void setIdCampoPlantilla(Integer idCampoPlantilla) {
        this.idCampoPlantilla = idCampoPlantilla;
    }
}