package com.certificados.app.controller;

import com.certificados.app.service.ArchivoDocumentoValidador;
import com.certificados.app.service.UsuarioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * GET /api/configuracion -> valores que el panel necesita mostrar y validar
 * (tipos de archivo aceptados, tamano maximo y largo minimo de las
 * contrasenas), para que no esten
 * escritos tambien en el JavaScript.
 */
@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionController {

    private final ArchivoDocumentoValidador validador;
    private final UsuarioService usuarioService;

    public ConfiguracionController(ArchivoDocumentoValidador validador, UsuarioService usuarioService) {
        this.validador = validador;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public Map<String, Object> obtener() {
        return Map.of(
                "extensionesPermitidas", validador.extensionesPermitidas(),
                "tamanoMaximoMb", ArchivoDocumentoValidador.TAMANO_MAXIMO / (1024 * 1024),
                "contrasenaMinima", usuarioService.contrasenaMinima()
        );
    }
}
