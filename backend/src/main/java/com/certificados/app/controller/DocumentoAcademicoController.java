package com.certificados.app.controller;

import com.certificados.app.model.DocumentoAcademico;
import com.certificados.app.service.DocumentoAcademicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documentos-academicos")
public class DocumentoAcademicoController {

    private final DocumentoAcademicoService service;

    public DocumentoAcademicoController(
            DocumentoAcademicoService service) {
        this.service = service;
    }

    @GetMapping
    public List<DocumentoAcademico> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public DocumentoAcademico buscarPorId(
            @PathVariable Integer id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/asignatura/{idAsignatura}")
    public List<DocumentoAcademico> listarPorAsignatura(
            @PathVariable Integer idAsignatura) {
        return service.listarPorAsignatura(idAsignatura);
    }

    @PostMapping
    public ResponseEntity<DocumentoAcademico> guardar(
            @Valid @RequestBody DocumentoAcademico documento) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.guardar(documento));
    }
}