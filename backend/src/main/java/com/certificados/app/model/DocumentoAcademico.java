package com.certificados.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
/*
 * Desde la migracion 05 una asignatura puede tener VARIOS documentos del
 * mismo tipo (por ejemplo dos cartas descriptivas de periodos distintos):
 * cada archivo que se monta queda como su propio documento, con su tipo y
 * su formato. Por eso ya no existe la unicidad asignatura + tipo.
 */
@Table(name = "CertificadosCursosAcademicosUPB_DocumentosAcademicosS")
public class DocumentoAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idDocumentoAcademico")
    private Integer id;

    @NotBlank(message = "El código de formato es obligatorio")
    @Column(name = "c_codigoFormato", length = 50)
    private String codigoFormato;

    @NotBlank(message = "La versión del formato es obligatoria")
    @Column(name = "c_versionFormato", length = 20)
    private String versionFormato;

    @NotNull(message = "La asignatura es obligatoria")
    @Column(name = "n_idAsignatura", nullable = false)
    private Integer idAsignatura;

    @NotNull(message = "El tipo de documento académico es obligatorio")
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