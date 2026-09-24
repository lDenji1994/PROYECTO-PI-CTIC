package com.certificados.app.controller;

import com.certificados.app.model.Asignatura;
import com.certificados.app.service.AsignaturaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asignaturas")
public class AsignaturaController {

    private final AsignaturaService service;

    public AsignaturaController(AsignaturaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Asignatura> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public Asignatura buscarPorId(
            @PathVariable Integer id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/codigo/{codigo}")
    public Asignatura buscarPorCodigo(
            @PathVariable String codigo) {
        return service.buscarPorCodigo(codigo);
    }

    @PostMapping
    public ResponseEntity<Asignatura> crear(
            @Valid @RequestBody Asignatura asignatura) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.crear(asignatura));
    }
}