package com.certificados.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VersionDocumentoDTO {

    private Integer id;
    private Integer idDocumentoAcademico;
    private String periodo;
    private String nombreArchivo;
    private String escuela;
    private String facultad;
    private String clasificacionCINE;
    private String nucleoBasicoConocimiento;
    private String ciclo;
    private String nivelFormacion;
    private BigDecimal  horasTeoricas;
    private BigDecimal horasPracticas;
    private BigDecimal horasLaboratorio;
    private BigDecimal horasIndependientes;
    private BigDecimal creditos;
    private String requisitos;
    private String justificacion;
    private String descripcion;
    private String proposito;
    private String modoCalificacion;
    private String modalidades;
    private String actaAprobacion;
    private String actaModificacion;
    private String observaciones;
    private Integer idProgramaDisena;
    private LocalDateTime fechaCarga;

    public VersionDocumentoDTO() {
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

    public BigDecimal getHorasPracticas() {
        return horasPracticas;
    }

    public void setHorasPracticas(BigDecimal horasPracticas) {
        this.horasPracticas = horasPracticas;
    }

    public BigDecimal getHorasLaboratorio() {
        return horasLaboratorio;
    }

    public void setHorasLaboratorio(BigDecimal horasLaboratorio) {
        this.horasLaboratorio = horasLaboratorio;
    }

    public BigDecimal getHorasIndependientes() {
        return horasIndependientes;
    }

    public void setHorasIndependientes(BigDecimal horasIndependientes) {
        this.horasIndependientes = horasIndependientes;
    }

    public BigDecimal getCreditos() {
        return creditos;
    }

    public void setCreditos(BigDecimal creditos) {
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
