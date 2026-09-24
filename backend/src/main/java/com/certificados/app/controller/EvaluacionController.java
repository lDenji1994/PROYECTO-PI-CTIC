package com.certificados.app.controller;

import com.certificados.app.model.Evaluacion;
import com.certificados.app.service.EvaluacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluaciones")
@CrossOrigin(origins = "http://localhost:4200")
public class EvaluacionController {

    private final EvaluacionService service;

    public EvaluacionController(EvaluacionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Evaluacion>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Evaluacion> buscarPorId(@PathVariable Integer id) {
        Evaluacion evaluacion = service.buscarPorId(id);

        if (evaluacion == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(evaluacion);
    }

    @GetMapping("/version-documento/{idVersionDocumento}")
    public ResponseEntity<List<Evaluacion>> listarPorVersionDocumento(
            @PathVariable Integer idVersionDocumento) {

        return ResponseEntity.ok(
                service.listarPorVersionDocumento(idVersionDocumento)
        );
    }
}