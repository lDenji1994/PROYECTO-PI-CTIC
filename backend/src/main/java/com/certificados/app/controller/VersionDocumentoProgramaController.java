package com.certificados.app.controller;

import com.certificados.app.model.VersionDocumentoPrograma;
import com.certificados.app.service.VersionDocumentoProgramaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/versiones-documentos-programas")
@CrossOrigin(origins = "http://localhost:4200")
public class VersionDocumentoProgramaController {

    private final VersionDocumentoProgramaService service;

    public VersionDocumentoProgramaController(
            VersionDocumentoProgramaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<VersionDocumentoPrograma>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{idVersionDocumento}/{idPrograma}")
    public ResponseEntity<VersionDocumentoPrograma> buscarPorId(
            @PathVariable Integer idVersionDocumento,
            @PathVariable Integer idPrograma) {

        VersionDocumentoPrograma relacion =
                service.buscarPorId(idVersionDocumento, idPrograma);

        if (relacion == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(relacion);
    }

    @GetMapping("/version-documento/{idVersionDocumento}")
    public ResponseEntity<List<VersionDocumentoPrograma>>
    listarPorVersionDocumento(
            @PathVariable Integer idVersionDocumento) {

        return ResponseEntity.ok(
                service.listarPorVersionDocumento(idVersionDocumento)
        );
    }

    @GetMapping("/programa/{idPrograma}")
    public ResponseEntity<List<VersionDocumentoPrograma>>
    listarPorPrograma(
            @PathVariable Integer idPrograma) {

        return ResponseEntity.ok(
                service.listarPorPrograma(idPrograma)
        );
    }
}