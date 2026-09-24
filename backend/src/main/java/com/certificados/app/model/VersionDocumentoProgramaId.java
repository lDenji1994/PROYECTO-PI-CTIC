package com.certificados.app.model;

import java.io.Serializable;
import java.util.Objects;

public class VersionDocumentoProgramaId implements Serializable {

    private Integer idVersionDocumento;
    private Integer idPrograma;

    public VersionDocumentoProgramaId() {
    }

    public VersionDocumentoProgramaId(Integer idVersionDocumento, Integer idPrograma) {
        this.idVersionDocumento = idVersionDocumento;
        this.idPrograma = idPrograma;
    }

    public Integer getIdVersionDocumento() {
        return idVersionDocumento;
    }

    public void setIdVersionDocumento(Integer idVersionDocumento) {
        this.idVersionDocumento = idVersionDocumento;
    }

    public Integer getIdPrograma() {
        return idPrograma;
    }

    public void setIdPrograma(Integer idPrograma) {
        this.idPrograma = idPrograma;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VersionDocumentoProgramaId)) return false;
        VersionDocumentoProgramaId that = (VersionDocumentoProgramaId) o;
        return Objects.equals(idVersionDocumento, that.idVersionDocumento)
                && Objects.equals(idPrograma, that.idPrograma);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idVersionDocumento, idPrograma);
    }
}