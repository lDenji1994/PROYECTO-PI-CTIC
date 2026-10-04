package com.certificados.app.dto;

import java.math.BigDecimal;

/**
 * DATOS DEL CURSO QUE LA AUXILIAR ESCRIBE (O COPIA Y PEGA) A MANO
 * al cargar una carta descriptiva o un syllabus.
 *
 * Decision del equipo: no hay lectura automatica de los documentos; la
 * informacion que necesita el certificado se registra manualmente.
 *
 * OBLIGATORIOS (los exige VersionDocumentoService.aplicarDatos):
 *   - descripcion       : descripcion del curso
 *   - detalleContenido  : contenido del curso, UN TEMA POR LINEA
 *   - creditos
 *   - al menos un valor de horas
 *
 * OPCIONALES: el resto de campos de la carta descriptiva.
 *
 * Cada campo corresponde a una columna de la tabla VersionesDocumentosS,
 * excepto detalleContenido, que se guarda en la tabla ContenidosS (una
 * fila por linea).
 *
 * SE PUEDE MODIFICAR: agregar campos opcionales (agregar tambien en
 * VersionDocumentoService.aplicarDatos / leerDatos y en el formulario).
 */
public class DatosCursoDTO {

    /* ---- obligatorios ---- */
    private String descripcion;
    private String detalleContenido;
    private BigDecimal creditos;
    private BigDecimal horasTeoricas;
    private BigDecimal horasPracticas;
    private BigDecimal horasLaboratorio;
    private BigDecimal horasIndependientes;

    /* ---- opcionales ---- */
    private String escuela;
    private String facultad;
    private String clasificacionCINE;
    private String nucleoBasicoConocimiento;
    private String ciclo;
    private String nivelFormacion;
    private String requisitos;
    private String justificacion;
    private String proposito;
    private String modoCalificacion;
    private String modalidades;
    private String observaciones;

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getDetalleContenido() { return detalleContenido; }
    public void setDetalleContenido(String detalleContenido) { this.detalleContenido = detalleContenido; }
    public BigDecimal getCreditos() { return creditos; }
    public void setCreditos(BigDecimal creditos) { this.creditos = creditos; }
    public BigDecimal getHorasTeoricas() { return horasTeoricas; }
    public void setHorasTeoricas(BigDecimal horasTeoricas) { this.horasTeoricas = horasTeoricas; }
    public BigDecimal getHorasPracticas() { return horasPracticas; }
    public void setHorasPracticas(BigDecimal horasPracticas) { this.horasPracticas = horasPracticas; }
    public BigDecimal getHorasLaboratorio() { return horasLaboratorio; }
    public void setHorasLaboratorio(BigDecimal horasLaboratorio) { this.horasLaboratorio = horasLaboratorio; }
    public BigDecimal getHorasIndependientes() { return horasIndependientes; }
    public void setHorasIndependientes(BigDecimal horasIndependientes) { this.horasIndependientes = horasIndependientes; }
    public String getEscuela() { return escuela; }
    public void setEscuela(String escuela) { this.escuela = escuela; }
    public String getFacultad() { return facultad; }
    public void setFacultad(String facultad) { this.facultad = facultad; }
    public String getClasificacionCINE() { return clasificacionCINE; }
    public void setClasificacionCINE(String clasificacionCINE) { this.clasificacionCINE = clasificacionCINE; }
    public String getNucleoBasicoConocimiento() { return nucleoBasicoConocimiento; }
    public void setNucleoBasicoConocimiento(String nucleoBasicoConocimiento) { this.nucleoBasicoConocimiento = nucleoBasicoConocimiento; }
    public String getCiclo() { return ciclo; }
    public void setCiclo(String ciclo) { this.ciclo = ciclo; }
    public String getNivelFormacion() { return nivelFormacion; }
    public void setNivelFormacion(String nivelFormacion) { this.nivelFormacion = nivelFormacion; }
    public String getRequisitos() { return requisitos; }
    public void setRequisitos(String requisitos) { this.requisitos = requisitos; }
    public String getJustificacion() { return justificacion; }
    public void setJustificacion(String justificacion) { this.justificacion = justificacion; }
    public String getProposito() { return proposito; }
    public void setProposito(String proposito) { this.proposito = proposito; }
    public String getModoCalificacion() { return modoCalificacion; }
    public void setModoCalificacion(String modoCalificacion) { this.modoCalificacion = modoCalificacion; }
    public String getModalidades() { return modalidades; }
    public void setModalidades(String modalidades) { this.modalidades = modalidades; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
