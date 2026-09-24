package com.certificados.app.controller;

import com.certificados.app.model.Contenido;
import com.certificados.app.service.ContenidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contenidos")
@CrossOrigin(origins = "http://localhost:4200")
public class ContenidoController {

    private final ContenidoService service;

    public ContenidoController(ContenidoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Contenido>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contenido> buscarPorId(@PathVariable Integer id) {
        Contenido contenido = service.buscarPorId(id);

        if (contenido == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(contenido);
    }

    @GetMapping("/version-documento/{idVersionDocumento}")
    public ResponseEntity<List<Contenido>> listarPorVersionDocumento(
            @PathVariable Integer idVersionDocumento) {

        return ResponseEntity.ok(
                service.listarPorVersionDocumento(idVersionDocumento)
        );
    }
}