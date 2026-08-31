package com.certificados.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EstudianteDTO {
    private Long id;

    @NotBlank(message = "El codigo estudiantil es obligatorio")
    private String codigoEstudiantil;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombres;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellidos;

    @Email(message = "Email invalido")
    private String email;

    private String programaAcademico;

    public EstudianteDTO() {
    }

    public EstudianteDTO(Long id, String codigoEstudiantil, String nombres, String apellidos, String email, String programaAcademico) {
        this.id = id;
        this.codigoEstudiantil = codigoEstudiantil;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.email = email;
        this.programaAcademico = programaAcademico;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoEstudiantil() {
        return codigoEstudiantil;
    }

    public void setCodigoEstudiantil(String codigoEstudiantil) {
        this.codigoEstudiantil = codigoEstudiantil;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProgramaAcademico() {
        return programaAcademico;
    }

    public void setProgramaAcademico(String programaAcademico) {
        this.programaAcademico = programaAcademico;
    }
}