package com.certificados.app.controller;

import com.certificados.app.model.CampoPlantilla;
import com.certificados.app.service.CampoPlantillaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campos-plantilla")
@CrossOrigin(origins = "http://localhost:4200")
public class CampoPlantillaController {

    private final CampoPlantillaService service;

    public CampoPlantillaController(
            CampoPlantillaService service) {

        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CampoPlantilla>> listarTodos() {
        return ResponseEntity.ok(
                service.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampoPlantilla> buscarPorId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                service.buscarPorId(id)
        );
    }

    @GetMapping("/version-plantilla/{idVersionPlantilla}")
    public ResponseEntity<List<CampoPlantilla>>
    listarPorVersionPlantilla(
            @PathVariable Integer idVersionPlantilla) {

        return ResponseEntity.ok(
                service.listarPorVersionPlantilla(
                        idVersionPlantilla
                )
        );
    }

    @GetMapping("/seccion-plantilla/{idSeccionPlantilla}")
    public ResponseEntity<List<CampoPlantilla>>
    listarPorSeccionPlantilla(
            @PathVariable Integer idSeccionPlantilla) {

        return ResponseEntity.ok(
                service.listarPorSeccionPlantilla(
                        idSeccionPlantilla
                )
        );
    }

    @PostMapping
    public ResponseEntity<CampoPlantilla> crear(
            @RequestParam String codigoCampo,
            @RequestParam String nombreCampo,
            @RequestParam CampoPlantilla.TipoDato tipoDato,
            @RequestParam String fuente,
            @RequestParam CampoPlantilla.MetodoObtencion metodoObtencion,
            @RequestParam(required = false) Boolean repetible,
            @RequestParam(required = false) String reglaObtencion,
            @RequestParam Integer idVersionPlantilla,
            @RequestParam(required = false) Integer idSeccionPlantilla) {

        return ResponseEntity.ok(
                service.crear(
                        codigoCampo,
                        nombreCampo,
                        tipoDato,
                        fuente,
                        metodoObtencion,
                        repetible,
                        reglaObtencion,
                        idVersionPlantilla,
                        idSeccionPlantilla
                )
        );
    }
}