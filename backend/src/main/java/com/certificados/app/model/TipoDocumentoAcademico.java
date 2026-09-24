package com.certificados.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "CertificadosCursosAcademicosUPB_TiposDocumentosAcademicosS")
public class TipoDocumentoAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idTipoDocumentoAcademico")
    private Integer id;

    @Column(name = "c_codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "t_nombre", nullable = false, unique = true, length = 150)
    private String nombre;

    public TipoDocumentoAcademico() {
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
}
