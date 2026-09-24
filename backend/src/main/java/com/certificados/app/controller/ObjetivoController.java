package com.certificados.app.controller;

import com.certificados.app.model.Objetivo;
import com.certificados.app.service.ObjetivoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/objetivos")
@CrossOrigin(origins = "http://localhost:4200")
public class ObjetivoController {

    private final ObjetivoService service;

    public ObjetivoController(ObjetivoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Objetivo>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Objetivo> buscarPorId(@PathVariable Integer id) {
        Objetivo objetivo = service.buscarPorId(id);

        if (objetivo == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(objetivo);
    }

    @GetMapping("/version-documento/{idVersionDocumento}")
    public ResponseEntity<List<Objetivo>> listarPorVersionDocumento(
            @PathVariable Integer idVersionDocumento) {

        return ResponseEntity.ok(
                service.listarPorVersionDocumento(idVersionDocumento)
        );
    }
}