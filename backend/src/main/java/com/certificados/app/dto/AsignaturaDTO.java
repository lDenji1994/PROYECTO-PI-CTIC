package com.certificados.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Asignatura tal como la maneja el panel.
 *
 * En los formatos institucionales (carta descriptiva, syllabus) el
 * codigo de una asignatura se compone de DOS codigos:
 *
 *     MATERIA  +  CURSO        ej.  FION 0001
 *
 * En la base de datos se sigue guardando en la unica columna c_codigo
 * como "MATERIA CURSO" (un espacio entre ambos), asi que NO hizo falta
 * cambiar el esquema SQL del equipo. Este DTO separa y une ambos codigos.
 *
 * Compatibilidad: si alguien envia solo "codigo" (como antes), el
 * servicio lo separa automaticamente.
 */
public class AsignaturaDTO {

    private Integer id;

    @Size(max = 6, message = "El código de materia no puede exceder 6 caracteres")
    private String codigoMateria;

    @Size(max = 8, message = "El código de curso no puede exceder 8 caracteres")
    private String codigoCurso;

    /** Codigo completo "MATERIA CURSO" (solo lectura para el panel). */
    @Size(max = 20, message = "El código no puede exceder 20 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre de la asignatura es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String nombre;

    public AsignaturaDTO() {
    }

    public AsignaturaDTO(Integer id, String codigoMateria, String codigoCurso, String codigo, String nombre) {
        this.id = id;
        this.codigoMateria = codigoMateria;
        this.codigoCurso = codigoCurso;
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCodigoMateria() { return codigoMateria; }
    public void setCodigoMateria(String codigoMateria) { this.codigoMateria = codigoMateria; }
    public String getCodigoCurso() { return codigoCurso; }
    public void setCodigoCurso(String codigoCurso) { this.codigoCurso = codigoCurso; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
