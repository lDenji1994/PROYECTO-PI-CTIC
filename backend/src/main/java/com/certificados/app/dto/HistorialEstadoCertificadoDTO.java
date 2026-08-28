package com.certificados.app.dto;

import com.certificados.app.model.EstadoCertificado;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialEstadoCertificadoDTO {
    private Long id;
    private Long certificadoId;
    private EstadoCertificado estadoAnterior;
    private EstadoCertificado estadoNuevo;
    private LocalDateTime fechaCambio;
}
