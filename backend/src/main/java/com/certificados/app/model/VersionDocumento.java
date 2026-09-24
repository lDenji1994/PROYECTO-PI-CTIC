package com.certificados.app.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

import java.time.LocalDateTime;

@Entity
@Table(name = "CertificadosCursosAcademicosUPB_VersionesDocumentosS")
public class VersionDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idVersionDocumento")
    private Integer id;

    @Column(name = "n_idDocumentoAcademico", nullable = false)
    private Integer idDocumentoAcademico;

    @Column(name = "c_periodo", length = 20)
    private String periodo;

    @Column(name = "t_nombreArchivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Lob
    @Column(name = "bi_archivo", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] archivo;

    @Column(name = "t_escuela", length = 150)
    private String escuela;

    @Column(name = "t_facultad", length = 150)
    private String facultad;

    @Column(name = "c_clasificacionCINE", length = 100)
    private String clasificacionCINE;

    @Column(name = "t_nucleoBasicoConocimiento", columnDefinition = "TEXT")
    private String nucleoBasicoConocimiento;

    @Column(name = "c_ciclo", length = 100)
    private String ciclo;

    @Column(name = "c_nivelFormacion", length = 100)
    private String nivelFormacion;

    @Column(name = "n_horasTeoricas")
    private BigDecimal  horasTeoricas;

    @Column(name = "n_horasPracticas")
    private BigDecimal  horasPracticas;

    @Column(name = "n_horasLaboratorio")
    private BigDecimal  horasLaboratorio;

    @Column(name = "n_horasIndependientes")
    private BigDecimal  horasIndependientes;

    @Column(name = "n_creditos")
    private BigDecimal  creditos;

    @Column(name = "t_requisitos", columnDefinition = "TEXT")
    private String requisitos;

    @Column(name = "t_justificacion", columnDefinition = "TEXT")
    private String justificacion;

    @Column(name = "t_descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "t_proposito", columnDefinition = "TEXT")
    private String proposito;

    @Column(name = "c_modoCalificacion", length = 100)
    private String modoCalificacion;

    @Column(name = "t_modalidades", columnDefinition = "TEXT")
    private String modalidades;

    @Column(name = "t_actaAprobacion", columnDefinition = "TEXT")
    private String actaAprobacion;

    @Column(name = "t_actaModificacion", columnDefinition = "TEXT")
    private String actaModificacion;

    @Column(name = "t_observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "n_idProgramaDisena")
    private Integer idProgramaDisena;

    @Column(name = "dt_fechaCarga", nullable = false)
    private LocalDateTime fechaCarga;

    @PrePersist
    protected void prePersist() {
        if (fechaCarga == null) {
            fechaCarga = LocalDateTime.now();
        }
    }

    public VersionDocumento() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdDocumentoAcademico() {
        return idDocumentoAcademico;
    }

    public void setIdDocumentoAcademico(Integer idDocumentoAcademico) {
        this.idDocumentoAcademico = idDocumentoAcademico;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public byte[] getArchivo() {
        return archivo;
    }

    public void setArchivo(byte[] archivo) {
        this.archivo = archivo;
    }

    public String getEscuela() {
        return escuela;
    }

    public void setEscuela(String escuela) {
        this.escuela = escuela;
    }

    public String getFacultad() {
        return facultad;
    }

    public void setFacultad(String facultad) {
        this.facultad = facultad;
    }

    public String getClasificacionCINE() {
        return clasificacionCINE;
    }

    public void setClasificacionCINE(String clasificacionCINE) {
        this.clasificacionCINE = clasificacionCINE;
    }

    public String getNucleoBasicoConocimiento() {
        return nucleoBasicoConocimiento;
    }

    public void setNucleoBasicoConocimiento(String nucleoBasicoConocimiento) {
        this.nucleoBasicoConocimiento = nucleoBasicoConocimiento;
    }

    public String getCiclo() {
        return ciclo;
    }

    public void setCiclo(String ciclo) {
        this.ciclo = ciclo;
    }

    public String getNivelFormacion() {
        return nivelFormacion;
    }

    public void setNivelFormacion(String nivelFormacion) {
        this.nivelFormacion = nivelFormacion;
    }

    public BigDecimal  getHorasTeoricas() {
        return horasTeoricas;
    }

    public void setHorasTeoricas(BigDecimal  horasTeoricas) {
        this.horasTeoricas = horasTeoricas;
    }

    public BigDecimal  getHorasPracticas() {
        return horasPracticas;
    }

    public void setHorasPracticas(BigDecimal  horasPracticas) {
        this.horasPracticas = horasPracticas;
    }

    public BigDecimal  getHorasLaboratorio() {
        return horasLaboratorio;
    }

    public void setHorasLaboratorio(BigDecimal  horasLaboratorio) {
        this.horasLaboratorio = horasLaboratorio;
    }

    public BigDecimal  getHorasIndependientes() {
        return horasIndependientes;
    }

    public void setHorasIndependientes(BigDecimal  horasIndependientes) {
        this.horasIndependientes = horasIndependientes;
    }

    public BigDecimal  getCreditos() {
        return creditos;
    }

    public void setCreditos(BigDecimal  creditos) {
        this.creditos = creditos;
    }

    public String getRequisitos() {
        return requisitos;
    }

    public void setRequisitos(String requisitos) {
        this.requisitos = requisitos;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getProposito() {
        return proposito;
    }

    public void setProposito(String proposito) {
        this.proposito = proposito;
    }

    public String getModoCalificacion() {
        return modoCalificacion;
    }

    public void setModoCalificacion(String modoCalificacion) {
        this.modoCalificacion = modoCalificacion;
    }

    public String getModalidades() {
        return modalidades;
    }

    public void setModalidades(String modalidades) {
        this.modalidades = modalidades;
    }

    public String getActaAprobacion() {
        return actaAprobacion;
    }

    public void setActaAprobacion(String actaAprobacion) {
        this.actaAprobacion = actaAprobacion;
    }

    public String getActaModificacion() {
        return actaModificacion;
    }

    public void setActaModificacion(String actaModificacion) {
        this.actaModificacion = actaModificacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Integer getIdProgramaDisena() {
        return idProgramaDisena;
    }

    public void setIdProgramaDisena(Integer idProgramaDisena) {
        this.idProgramaDisena = idProgramaDisena;
    }

    public LocalDateTime getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(LocalDateTime fechaCarga) {
        this.fechaCarga = fechaCarga;
    }
}
