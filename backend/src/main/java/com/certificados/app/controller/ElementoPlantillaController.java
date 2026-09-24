package com.certificados.app.controller;

import com.certificados.app.model.ElementoPlantilla;
import com.certificados.app.service.ElementoPlantillaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/elementos-plantilla")
@CrossOrigin(origins = "http://localhost:4200")
public class ElementoPlantillaController {

    private final ElementoPlantillaService service;

    public ElementoPlantillaController(
            ElementoPlantillaService service) {

        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ElementoPlantilla>> listarTodos() {
        return ResponseEntity.ok(
                service.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ElementoPlantilla> buscarPorId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                service.buscarPorId(id)
        );
    }

    @GetMapping("/version-plantilla/{idVersionPlantilla}")
    public ResponseEntity<List<ElementoPlantilla>>
    listarPorVersionPlantilla(
            @PathVariable Integer idVersionPlantilla) {

        return ResponseEntity.ok(
                service.listarPorVersionPlantilla(
                        idVersionPlantilla
                )
        );
    }

    @GetMapping("/seccion-plantilla/{idSeccionPlantilla}")
    public ResponseEntity<List<ElementoPlantilla>>
    listarPorSeccionPlantilla(
            @PathVariable Integer idSeccionPlantilla) {

        return ResponseEntity.ok(
                service.listarPorSeccionPlantilla(
                        idSeccionPlantilla
                )
        );
    }

    @GetMapping("/campo-plantilla/{idCampoPlantilla}")
    public ResponseEntity<List<ElementoPlantilla>>
    listarPorCampoPlantilla(
            @PathVariable Integer idCampoPlantilla) {

        return ResponseEntity.ok(
                service.listarPorCampoPlantilla(
                        idCampoPlantilla
                )
        );
    }

    @PostMapping
    public ResponseEntity<ElementoPlantilla> crear(
            @RequestParam ElementoPlantilla.TipoElemento tipoElemento,
            @RequestParam(required = false) String contenido,
            @RequestParam(required = false) BigDecimal posicionX,
            @RequestParam(required = false) BigDecimal posicionY,
            @RequestParam(required = false) BigDecimal ancho,
            @RequestParam(required = false) BigDecimal alto,
            @RequestParam(required = false) BigDecimal tamanoFuente,
            @RequestParam(required = false) String tipoFuente,
            @RequestParam(required = false) String alineacion,
            @RequestParam(required = false) Integer orden,
            @RequestParam Integer idVersionPlantilla,
            @RequestParam(required = false) Integer idSeccionPlantilla,
            @RequestParam(required = false) Integer idCampoPlantilla) {

        return ResponseEntity.ok(
                service.crear(
                        tipoElemento,
                        contenido,
                        posicionX,
                        posicionY,
                        ancho,
                        alto,
                        tamanoFuente,
                        tipoFuente,
                        alineacion,
                        orden,
                        idVersionPlantilla,
                        idSeccionPlantilla,
                        idCampoPlantilla
                )
        );
    }
}