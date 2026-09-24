package com.certificados.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "CertificadosCursosAcademicosUPB_VersionesDocumentosProgramasS")
@IdClass(VersionDocumentoProgramaId.class)
public class VersionDocumentoPrograma {

    @Id
    @Column(name = "n_idVersionDocumento", nullable = false)
    private Integer idVersionDocumento;

    @Id
    @Column(name = "n_idPrograma", nullable = false)
    private Integer idPrograma;

    public VersionDocumentoPrograma() {
    }

    public VersionDocumentoPrograma(
            Integer idVersionDocumento,
            Integer idPrograma) {
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
}