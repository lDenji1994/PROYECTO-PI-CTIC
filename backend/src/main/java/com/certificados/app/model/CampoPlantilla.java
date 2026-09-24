package com.certificados.app.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "CertificadosCursosAcademicosUPB_CamposPlantillas",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_VersionPlantilla_CodigoCampo",
            columnNames = {"n_idVersionPlantilla", "c_codigoCampo"}
        )
    }
)
public class CampoPlantilla {

    public enum TipoDato {
        TEXTO,
        NUMERO,
        FECHA,
        BOOLEANO
    }

    public enum MetodoObtencion {
        EXTRAER,
        CALCULAR,
        TRANSFORMAR
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idCampoPlantilla")
    private Integer id;

    @Column(name = "c_codigoCampo", nullable = false, length = 100)
    private String codigoCampo;

    @Column(name = "t_nombreCampo", nullable = false, length = 150)
    private String nombreCampo;

    @Enumerated(EnumType.STRING)
    @Column(name = "t_tipoDato", nullable = false)
    private TipoDato tipoDato;

    @Column(name = "c_fuente", nullable = false, length = 50)
    private String fuente;

    @Enumerated(EnumType.STRING)
    @Column(name = "c_metodoObtencion", nullable = false)
    private MetodoObtencion metodoObtencion;

    @Column(name = "b_repetible", nullable = false)
    private Boolean repetible = false;

    @Column(name = "t_reglaObtencion", columnDefinition = "TEXT")
    private String reglaObtencion;

    @Column(name = "n_idVersionPlantilla", nullable = false)
    private Integer idVersionPlantilla;

    @Column(name = "n_idSeccionPlantilla")
    private Integer idSeccionPlantilla;

    public CampoPlantilla() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigoCampo() {
        return codigoCampo;
    }

    public void setCodigoCampo(String codigoCampo) {
        this.codigoCampo = codigoCampo;
    }

    public String getNombreCampo() {
        return nombreCampo;
    }

    public void setNombreCampo(String nombreCampo) {
        this.nombreCampo = nombreCampo;
    }

    public TipoDato getTipoDato() {
        return tipoDato;
    }

    public void setTipoDato(TipoDato tipoDato) {
        this.tipoDato = tipoDato;
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    public MetodoObtencion getMetodoObtencion() {
        return metodoObtencion;
    }

    public void setMetodoObtencion(MetodoObtencion metodoObtencion) {
        this.metodoObtencion = metodoObtencion;
    }

    public Boolean getRepetible() {
        return repetible;
    }

    public void setRepetible(Boolean repetible) {
        this.repetible = repetible;
    }

    public String getReglaObtencion() {
        return reglaObtencion;
    }

    public void setReglaObtencion(String reglaObtencion) {
        this.reglaObtencion = reglaObtencion;
    }

    public Integer getIdVersionPlantilla() {
        return idVersionPlantilla;
    }

    public void setIdVersionPlantilla(Integer idVersionPlantilla) {
        this.idVersionPlantilla = idVersionPlantilla;
    }

    public Integer getIdSeccionPlantilla() {
        return idSeccionPlantilla;
    }

    public void setIdSeccionPlantilla(Integer idSeccionPlantilla) {
        this.idSeccionPlantilla = idSeccionPlantilla;
    }
}