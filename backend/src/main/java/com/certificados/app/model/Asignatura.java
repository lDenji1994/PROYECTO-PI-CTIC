package com.certificados.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "CertificadosCursosAcademicosUPB_AsignaturasS")
public class Asignatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idAsignatura")
    private Integer id;

    @NotBlank(message = "El código de la asignatura es obligatorio")
    @Column(name = "c_codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @NotBlank(message = "El nombre de la asignatura es obligatorio")
    @Column(name = "t_nombre", nullable = false, length = 150)
    private String nombre;

    public Asignatura() {
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