package com.certificados.app.controller;

import com.certificados.app.model.TipoCertificado;
import com.certificados.app.repository.TipoCertificadoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-certificados")
@CrossOrigin(origins = "http://localhost:4200")
public class TipoCertificadoController {

    private final TipoCertificadoRepository repository;

    public TipoCertificadoController(TipoCertificadoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<TipoCertificado>> listarTodos() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoCertificado> buscarPorId(@PathVariable Integer id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}