package com.certificados.app.dto;

public class InformacionDocumentoCertificadoDTO {

    private Integer idAsignatura;
    private Integer idDocumentoAcademico;
    private Integer idVersionDocumento;

    private String codigoCampo;
    private String nombreCampo;
    private String fuente;
    private String metodoObtencion;
    private String reglaObtencion;

    private Object valor;
    private boolean disponible;

    public InformacionDocumentoCertificadoDTO() {
    }

    public Integer getIdAsignatura() {
        return idAsignatura;
    }

    public void setIdAsignatura(Integer idAsignatura) {
        this.idAsignatura = idAsignatura;
    }

    public Integer getIdDocumentoAcademico() {
        return idDocumentoAcademico;
    }

    public void setIdDocumentoAcademico(Integer idDocumentoAcademico) {
        this.idDocumentoAcademico = idDocumentoAcademico;
    }

    public Integer getIdVersionDocumento() {
        return idVersionDocumento;
    }

    public void setIdVersionDocumento(Integer idVersionDocumento) {
        this.idVersionDocumento = idVersionDocumento;
    }

    public String getCodigoCampo() {
        return codigoCampo;
    }

    public void setCodigoCampo(String codigoCampo) {
        this.codigoCampo = codigoCampo;
    }

    public String getNombreCampo() {
        return nombreCampo;
    }

    public void setNombreCampo(String nombreCampo) {
        this.nombreCampo = nombreCampo;
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    public String getMetodoObtencion() {
        return metodoObtencion;
    }

    public void setMetodoObtencion(String metodoObtencion) {
        this.metodoObtencion = metodoObtencion;
    }

    public String getReglaObtencion() {
        return reglaObtencion;
    }

    public void setReglaObtencion(String reglaObtencion) {
        this.reglaObtencion = reglaObtencion;
    }

    public Object getValor() {
        return valor;
    }

    public void setValor(Object valor) {
        this.valor = valor;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
}