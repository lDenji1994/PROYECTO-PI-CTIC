package com.certificados.app.controller;

import com.certificados.app.model.SeccionPlantilla;
import com.certificados.app.service.SeccionPlantillaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/secciones-plantilla")
@CrossOrigin(origins = "http://localhost:4200")
public class SeccionPlantillaController {

    private final SeccionPlantillaService service;

    public SeccionPlantillaController(
            SeccionPlantillaService service) {

        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<SeccionPlantilla>> listarTodos() {
        return ResponseEntity.ok(
                service.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeccionPlantilla> buscarPorId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                service.buscarPorId(id)
        );
    }

    @GetMapping("/version-plantilla/{idVersionPlantilla}")
    public ResponseEntity<List<SeccionPlantilla>>
    listarPorVersionPlantilla(
            @PathVariable Integer idVersionPlantilla) {

        return ResponseEntity.ok(
                service.listarPorVersionPlantilla(
                        idVersionPlantilla
                )
        );
    }

    @PostMapping
    public ResponseEntity<SeccionPlantilla> crear(
            @RequestParam String codigoSeccion,
            @RequestParam String nombreSeccion,
            @RequestParam(required = false) Boolean repetible,
            @RequestParam Integer orden,
            @RequestParam Integer idVersionPlantilla) {

        return ResponseEntity.ok(
                service.crear(
                        codigoSeccion,
                        nombreSeccion,
                        repetible,
                        orden,
                        idVersionPlantilla
                )
        );
    }
}