package com.certificados.app.controller;

import com.certificados.app.dto.FormatoDTO;
import com.certificados.app.service.FormatoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** GET /api/formatos -> catalogo de formatos (con cual es el vigente). */
@RestController
@RequestMapping("/api/formatos")
public class FormatoController {

    private final FormatoService service;

    public FormatoController(FormatoService service) {
        this.service = service;
    }

    @GetMapping
    public List<FormatoDTO> listar() {
        return service.listar();
    }
}
