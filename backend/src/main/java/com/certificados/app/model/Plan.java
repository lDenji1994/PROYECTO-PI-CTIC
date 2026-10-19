package com.certificados.app.model;

import jakarta.persistence.*;

/**
 * PLAN DE ESTUDIOS de un programa (tabla planes, migracion 07).
 *
 * Un programa puede tener varios planes (versiones), por ejemplo
 * "Plan 2003" (antiguo) y "Plan 2026" (vigente). Cada plan tiene un nombre
 * editable, su estado y sus asignaturas por semestre (tabla planes_estudio).
 *
 * NO MODIFICAR los nombres de columnas (deben coincidir con la base de datos).
 */
@Entity
@Table(name = "planes")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idPlan")
    private Integer id;

    @Column(name = "n_idPrograma", nullable = false)
    private Integer idPrograma;

    @Column(name = "t_nombre", nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "c_estado", nullable = false, length = 20)
    private EstadoPlanEstudio estado = EstadoPlanEstudio.VIGENTE;

    public Plan() {
    }

    public Plan(Integer idPrograma, String nombre, EstadoPlanEstudio estado) {
        this.idPrograma = idPrograma;
        this.nombre = nombre;
        this.estado = estado;
    }

    public Integer getId() {
        return id;
    }

    public Integer getIdPrograma() {
        return idPrograma;
    }

    public void setIdPrograma(Integer idPrograma) {
        this.idPrograma = idPrograma;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public EstadoPlanEstudio getEstado() {
        return estado;
    }

    public void setEstado(EstadoPlanEstudio estado) {
        this.estado = estado;
    }
}
