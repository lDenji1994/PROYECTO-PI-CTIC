package com.certificados.app.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "CertificadosCursosAcademicosUPB_SeccionesPlantillas",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_VersionPlantilla_CodigoSeccion",
            columnNames = {"n_idVersionPlantilla", "c_codigoSeccion"}
        )
    }
)
public class SeccionPlantilla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idSeccionPlantilla")
    private Integer id;

    @Column(name = "c_codigoSeccion", nullable = false, length = 100)
    private String codigoSeccion;

    @Column(name = "t_nombreSeccion", nullable = false, length = 150)
    private String nombreSeccion;

    @Column(name = "b_repetible", nullable = false)
    private Boolean repetible = false;

    @Column(name = "n_orden")
    private Integer orden;

    @Column(name = "n_idVersionPlantilla", nullable = false)
    private Integer idVersionPlantilla;

    public SeccionPlantilla() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigoSeccion() {
        return codigoSeccion;
    }

    public void setCodigoSeccion(String codigoSeccion) {
        this.codigoSeccion = codigoSeccion;
    }

    public String getNombreSeccion() {
        return nombreSeccion;
    }

    public void setNombreSeccion(String nombreSeccion) {
        this.nombreSeccion = nombreSeccion;
    }

    public Boolean getRepetible() {
        return repetible;
    }

    public void setRepetible(Boolean repetible) {
        this.repetible = repetible;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public Integer getIdVersionPlantilla() {
        return idVersionPlantilla;
    }

    public void setIdVersionPlantilla(Integer idVersionPlantilla) {
        this.idVersionPlantilla = idVersionPlantilla;
    }
}
