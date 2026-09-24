package com.certificados.app.model;

import jakarta.persistence.*;

@Entity
@Table(
        name = "CertificadosCursosAcademicosUPB_UsuariosS",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "t_usuario",
                        columnNames = "t_usuario"
                ),
                @UniqueConstraint(
                        name = "t_correo",
                        columnNames = "t_correo"
                )
        }
)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "n_idUsuario")
    private Integer id;

    @Column(
            name = "t_nombreCompleto",
            nullable = false,
            length = 150
    )
    private String nombreCompleto;

    @Column(
            name = "t_usuario",
            nullable = false,
            unique = true,
            length = 100
    )
    private String usuario;

    @Column(
            name = "t_correo",
            unique = true,
            length = 150
    )
    private String correo;

    @Column(
            name = "t_contrasena",
            nullable = false,
            length = 255
    )
    private String contrasena;

    @Column(
            name = "b_activo",
            nullable = false
    )
    private Boolean activo = true;

    @Column(
            name = "n_idRol",
            nullable = false
    )
    private Integer idRol;

    public Usuario() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }
}
