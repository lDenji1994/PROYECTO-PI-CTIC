package com.certificados.app.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "CertificadosCursosAcademicosUPB_RolesS",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "c_nombre",
                        columnNames = "c_nombre"
                )
        }
)
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idRol")
    private Integer id;

    @Column(
            name = "c_nombre",
            nullable = false,
            unique = true,
            length = 50
    )
    private String nombre;

    @Column(
            name = "t_descripcion",
            length = 150
    )
    private String descripcion;

    public Rol() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}

