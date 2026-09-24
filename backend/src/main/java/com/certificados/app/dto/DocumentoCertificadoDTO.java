package com.certificados.app.dto;

public class DocumentoCertificadoDTO {

    private Integer idAsignatura;

    private Integer idDocumentoAcademico;

    private Integer idTipoDocumentoAcademico;

    private String tipoDocumento;

    private String codigoFormato;

    private String versionFormato;

    private boolean disponible;

    private int versionesDisponibles;

    public DocumentoCertificadoDTO() {
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

    public Integer getIdTipoDocumentoAcademico() {
        return idTipoDocumentoAcademico;
    }

    public void setIdTipoDocumentoAcademico(
            Integer idTipoDocumentoAcademico) {

        this.idTipoDocumentoAcademico =
                idTipoDocumentoAcademico;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getCodigoFormato() {
        return codigoFormato;
    }

    public void setCodigoFormato(String codigoFormato) {
        this.codigoFormato = codigoFormato;
    }

    public String getVersionFormato() {
        return versionFormato;
    }

    public void setVersionFormato(String versionFormato) {
        this.versionFormato = versionFormato;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public int getVersionesDisponibles() {
        return versionesDisponibles;
    }

    public void setVersionesDisponibles(
            int versionesDisponibles) {

        this.versionesDisponibles =
                versionesDisponibles;
    }
}