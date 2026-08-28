package com.certificados.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Registro de auditoria: cada fila representa UN cambio de estado
 * de un Certificado (ej. PENDIENTE -> EMITIDO). No se actualiza ni
 * se borra nunca despues de creado, solo se inserta (US-21.05:
 * el historial no debe poder modificarse).
 */
@Entity
@Table(name = "historial_estados_certificado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialEstadoCertificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificado_id", nullable = false)
    private Certificado certificado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCertificado estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCertificado estadoNuevo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCambio = LocalDateTime.now();
}
