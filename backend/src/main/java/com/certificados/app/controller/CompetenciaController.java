package com.certificados.app.controller;

import com.certificados.app.model.Competencia;
import com.certificados.app.service.CompetenciaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/competencias")
@CrossOrigin(origins = "http://localhost:4200")
public class CompetenciaController {

    private final CompetenciaService service;

    public CompetenciaController(CompetenciaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Competencia>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Competencia> buscarPorId(@PathVariable Integer id) {
        Competencia competencia = service.buscarPorId(id);

        if (competencia == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(competencia);
    }

    @GetMapping("/version-documento/{idVersionDocumento}")
    public ResponseEntity<List<Competencia>> listarPorVersionDocumento(
            @PathVariable Integer idVersionDocumento) {

        return ResponseEntity.ok(
                service.listarPorVersionDocumento(idVersionDocumento)
        );
    }
}