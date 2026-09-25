package com.certificados.app.dto;

import java.util.List;

public class AsignaturaConsultaDTO {
    private String codigoAsignatura;
    private String nombreAsignatura;
    private boolean encontrada;
    private List<VersionDocumentoDTO> documentosAsociados;

    public AsignaturaConsultaDTO() {}

    public AsignaturaConsultaDTO(String codigoAsignatura, String nombreAsignatura, boolean encontrada, List<VersionDocumentoDTO> documentosAsociados) {
        this.codigoAsignatura = codigoAsignatura;
        this.nombreAsignatura = nombreAsignatura;
        this.encontrada = encontrada;
        this.documentosAsociados = documentosAsociados;
    }

    public String getCodigoAsignatura() { return codigoAsignatura; }
    public void setCodigoAsignatura(String codigoAsignatura) { this.codigoAsignatura = codigoAsignatura; }

    public String getNombreAsignatura() { return nombreAsignatura; }
    public void setNombreAsignatura(String nombreAsignatura) { this.nombreAsignatura = nombreAsignatura; }

    public boolean isEncontrada() { return encontrada; }
    public void setEncontrada(boolean encontrada) { this.encontrada = encontrada; }

    public List<VersionDocumentoDTO> getDocumentosAsociados() { return documentosAsociados; }
    public void setDocumentosAsociados(List<VersionDocumentoDTO> documentosAsociados) { this.documentosAsociados = documentosAsociados; }
}