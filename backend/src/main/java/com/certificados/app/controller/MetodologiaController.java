package com.certificados.app.controller;

import com.certificados.app.model.Metodologia;
import com.certificados.app.service.MetodologiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metodologias")
@CrossOrigin(origins = "http://localhost:4200")
public class MetodologiaController {

    private final MetodologiaService service;

    public MetodologiaController(MetodologiaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Metodologia>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Metodologia> buscarPorId(@PathVariable Integer id) {
        Metodologia metodologia = service.buscarPorId(id);

        if (metodologia == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(metodologia);
    }

    @GetMapping("/version-documento/{idVersionDocumento}")
    public ResponseEntity<List<Metodologia>> listarPorVersionDocumento(
            @PathVariable Integer idVersionDocumento) {

        return ResponseEntity.ok(
                service.listarPorVersionDocumento(idVersionDocumento)
        );
    }
}