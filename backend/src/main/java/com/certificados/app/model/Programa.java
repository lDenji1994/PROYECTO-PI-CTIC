package com.certificados.app.model;

import jakarta.persistence.*;

import java.util.Objects;

/**
 * Módulo de Programas Académicos.
 *
 * Representa los programas académicos registrados en la plataforma.
 * Estos programas pueden relacionarse posteriormente con las versiones
 * de documentos académicos mediante VersionesDocumentosProgramasS.
 *
 * La entidad se encuentra alineada con la tabla:
 * CertificadosCursosAcademicosUPB_ProgramasS
 */
@Entity
@Table(name = "CertificadosCursosAcademicosUPB_ProgramasS")
public class Programa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idPrograma")
    private Integer id;

    @Column(name = "c_codigo", length = 20, unique = true)
    private String codigo;

    @Column(name = "t_nombre", length = 150, nullable = false)
    private String nombre;

    public Programa() {
    }

    public Programa(Integer id, String codigo, String nombre) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Programa programa = (Programa) o;
        return id != null && Objects.equals(id, programa.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
