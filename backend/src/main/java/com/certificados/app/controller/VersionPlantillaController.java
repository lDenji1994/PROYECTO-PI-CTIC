package com.certificados.app.controller;

import com.certificados.app.model.VersionPlantilla;
import com.certificados.app.service.VersionPlantillaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/versiones-plantilla")
@CrossOrigin(origins = "http://localhost:4200")
public class VersionPlantillaController {

    private final VersionPlantillaService service;

    public VersionPlantillaController(
            VersionPlantillaService service) {

        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<VersionPlantilla>> listarTodos() {
        return ResponseEntity.ok(
                service.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<VersionPlantilla> buscarPorId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                service.buscarPorId(id)
        );
    }

    @GetMapping("/plantilla/{idPlantillaCertificado}")
    public ResponseEntity<List<VersionPlantilla>>
    listarPorPlantilla(
            @PathVariable Integer idPlantillaCertificado) {

        return ResponseEntity.ok(
                service.listarPorPlantilla(
                        idPlantillaCertificado
                )
        );
    }

    @PostMapping
    public ResponseEntity<VersionPlantilla> crear(
            @RequestParam String versionFormato,
            @RequestParam Integer idPlantillaCertificado) {

        return ResponseEntity.ok(
                service.crear(
                        versionFormato,
                        idPlantillaCertificado
                )
        );
    }
}