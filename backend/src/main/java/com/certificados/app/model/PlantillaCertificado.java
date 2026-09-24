package com.certificados.app.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "CertificadosCursosAcademicosUPB_PlantillasCertificadosS",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "t_nombre",
                        columnNames = "t_nombre"
                )
        }
)
public class PlantillaCertificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idPlantillaCertificado")
    private Integer id;

    @Column(
            name = "t_nombre",
            nullable = false,
            length = 150
    )
    private String nombre;

    @Column(
            name = "n_idTipoCertificado",
            nullable = false
    )
    private Integer idTipoCertificado;

    public PlantillaCertificado() {
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

    public Integer getIdTipoCertificado() {
        return idTipoCertificado;
    }

    public void setIdTipoCertificado(Integer idTipoCertificado) {
        this.idTipoCertificado = idTipoCertificado;
    }
}