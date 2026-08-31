package com.certificados.app.dto;

import java.time.LocalDateTime;

public class CargaArchivoResponseDTO {

    private String id;
    private String nombreOriginal;
    private String tipoDocumento;
    private long tamanoBytes;
    private String mensaje;
    private LocalDateTime fechaCarga;

    public CargaArchivoResponseDTO() {
    }

    public CargaArchivoResponseDTO(String id, String nombreOriginal, String tipoDocumento, long tamanoBytes, String mensaje, LocalDateTime fechaCarga) {
        this.id = id;
        this.nombreOriginal = nombreOriginal;
        this.tipoDocumento = tipoDocumento;
        this.tamanoBytes = tamanoBytes;
        this.mensaje = mensaje;
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

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(LocalDateTime fechaCarga) {
        this.fechaCarga = fechaCarga;
    }
}