package com.certificados.app.controller;

import com.certificados.app.model.TipoDocumentoAcademico;
import com.certificados.app.service.TipoDocumentoAcademicoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-documentos-academicos")
@CrossOrigin(origins = "http://localhost:4200")
public class TipoDocumentoAcademicoController {

    private final TipoDocumentoAcademicoService service;

    public TipoDocumentoAcademicoController(TipoDocumentoAcademicoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<TipoDocumentoAcademico>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoDocumentoAcademico> buscarPorId(@PathVariable Integer id) {
        TipoDocumentoAcademico tipo = service.buscarPorId(id);

        if (tipo == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(tipo);
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<TipoDocumentoAcademico> buscarPorCodigo(
            @PathVariable String codigo) {

        TipoDocumentoAcademico tipo = service.buscarPorCodigo(codigo);

        if (tipo == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(tipo);
    }
}