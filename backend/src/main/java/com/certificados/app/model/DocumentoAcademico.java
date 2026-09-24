package com.certificados.app.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "CertificadosCursosAcademicosUPB_DocumentosAcademicosS",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_Asignatura_TipoDocumento",
                        columnNames = {
                                "n_idAsignatura",
                                "n_idTipoDocumentoAcademico"
                        }
                )
        }
)
public class DocumentoAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idDocumentoAcademico")
    private Integer id;

    @Column(name = "c_codigoFormato", length = 50)
    private String codigoFormato;

    @Column(name = "c_versionFormato", length = 20)
    private String versionFormato;

    @Column(name = "n_idAsignatura", nullable = false)
    private Integer idAsignatura;

    @Column(name = "n_idTipoDocumentoAcademico", nullable = false)
    private Integer idTipoDocumentoAcademico;

    public DocumentoAcademico() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getIdAsignatura() {
        return idAsignatura;
    }

    public void setIdAsignatura(Integer idAsignatura) {
        this.idAsignatura = idAsignatura;
    }

    public Integer getIdTipoDocumentoAcademico() {
        return idTipoDocumentoAcademico;
    }

    public void setIdTipoDocumentoAcademico(Integer idTipoDocumentoAcademico) {
        this.idTipoDocumentoAcademico = idTipoDocumentoAcademico;
    }
}
