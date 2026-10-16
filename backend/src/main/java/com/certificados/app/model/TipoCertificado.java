package com.certificados.app.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "tipos_certificado",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "t_nombre",
                        columnNames = "t_nombre"
                )
        }
)
public class TipoCertificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idTipoCertificado")
    private Integer id;

    @Column(
            name = "t_nombre",
            nullable = false,
            unique = true,
            length = 150
    )
    private String nombre;

    public TipoCertificado() {
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
}
