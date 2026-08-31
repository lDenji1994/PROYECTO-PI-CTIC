package com.certificados.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "documentos")
public class Documento {

    @Id
    private String id;
    private String nombreOriginal;
    private String rutaAlmacenamiento;
    private String tipoDocumento;
    private long tamanoBytes;
    private LocalDateTime fechaCarga;

    public Documento() {
    }

    public Documento(String id, String nombreOriginal, String rutaAlmacenamiento, String tipoDocumento, long tamanoBytes, LocalDateTime fechaCarga) {
        this.id = id;
        this.nombreOriginal = nombreOriginal;
        this.rutaAlmacenamiento = rutaAlmacenamiento;
        this.tipoDocumento = tipoDocumento;
        this.tamanoBytes = tamanoBytes;
        this.fechaCarga = fechaCarga;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public void setNombreOriginal(String nombreOriginal) {
        this.nombreOriginal = nombreOriginal;
    }

    public String getRutaAlmacenamiento() {
        return rutaAlmacenamiento;
    }

    public void setRutaAlmacenamiento(String rutaAlmacenamiento) {
        this.rutaAlmacenamiento = rutaAlmacenamiento;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public long getTamanoBytes() {
        return tamanoBytes;
    }

    public void setTamanoBytes(long tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }

    public LocalDateTime getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(LocalDateTime fechaCarga) {
        this.fechaCarga = fechaCarga;
    }
}