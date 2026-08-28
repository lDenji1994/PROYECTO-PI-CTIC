package com.certificados.app.dto;

import com.certificados.app.model.TipoDocumento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CargaArchivoResponseDTO {
    private String id;
    private String nombreOriginal;
    private TipoDocumento tipoDocumento;
    private long tamanoBytes;
    private String mensaje;
    private LocalDateTime fechaCarga;
}