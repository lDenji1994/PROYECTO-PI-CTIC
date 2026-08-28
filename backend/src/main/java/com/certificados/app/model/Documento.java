package com.certificados.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "documentos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Documento {

    @Id
    private String id;
    private String nombreOriginal;
    private String rutaArchivo; 

    @Enumerated(EnumType.STRING) 
    private TipoDocumento tipoDocumento;

    private long tamanoBytes;
    private LocalDateTime fechaCarga;
}