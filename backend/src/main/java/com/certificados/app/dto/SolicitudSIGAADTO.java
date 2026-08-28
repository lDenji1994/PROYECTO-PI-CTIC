package com.certificados.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudSIGAADTO {

    @NotBlank(message = "El identificador de la solicitud SIGAA es obligatorio")
    private String solicitudId;

    @NotBlank(message = "El código del estudiante es obligatorio")
    private String codigoEstudiantil;

    @NotNull(message = "El tipo de certificado es obligatorio")
    private String tipoCertificado;

    private String observaciones;

    private String estado;

    private String fechaRecepcion;
}
