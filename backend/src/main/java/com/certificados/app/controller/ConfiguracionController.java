package com.certificados.app.controller;

import com.certificados.app.service.ArchivoDocumentoValidador;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * GET /api/configuracion -> valores que el panel necesita mostrar y validar
 * (que tipos de archivo se aceptan y el tamano maximo), para que no esten
 * escritos tambien en el JavaScript.
 */
@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionController {

    private final ArchivoDocumentoValidador validador;

    public ConfiguracionController(ArchivoDocumentoValidador validador) {
        this.validador = validador;
    }

    @GetMapping
    public Map<String, Object> obtener() {
        return Map.of(
                "extensionesPermitidas", validador.extensionesPermitidas(),
                "tamanoMaximoMb", ArchivoDocumentoValidador.TAMANO_MAXIMO / (1024 * 1024)
        );
    }
}
