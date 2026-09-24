package com.certificados.app.controller;

import com.certificados.app.model.PlantillaCertificado;
import com.certificados.app.service.PlantillaCertificadoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plantillas-certificados")
@CrossOrigin(origins = "http://localhost:4200")
public class PlantillaCertificadoController {

    private final PlantillaCertificadoService service;

    public PlantillaCertificadoController(
            PlantillaCertificadoService service) {

        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<PlantillaCertificado>> listarTodos() {
        return ResponseEntity.ok(
                service.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantillaCertificado> buscarPorId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                service.buscarPorId(id)
        );
    }

    @PostMapping
    public ResponseEntity<PlantillaCertificado> crear(
            @RequestParam String nombre,
            @RequestParam Integer idTipoCertificado) {

        return ResponseEntity.ok(
                service.crear(
                        nombre,
                        idTipoCertificado
                )
        );
    }
}