package com.certificados.app.controller;

import com.certificados.app.model.CriterioCompetencia;
import com.certificados.app.service.CriterioCompetenciaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/criterios-competencia")
@CrossOrigin(origins = "http://localhost:4200")
public class CriterioCompetenciaController {

    private final CriterioCompetenciaService service;

    public CriterioCompetenciaController(
            CriterioCompetenciaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<CriterioCompetencia>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CriterioCompetencia> buscarPorId(
            @PathVariable Integer id) {

        CriterioCompetencia criterio = service.buscarPorId(id);

        if (criterio == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(criterio);
    }

    @GetMapping("/competencia/{idCompetencia}")
    public ResponseEntity<List<CriterioCompetencia>> listarPorCompetencia(
            @PathVariable Integer idCompetencia) {

        return ResponseEntity.ok(
                service.listarPorCompetencia(idCompetencia)
        );
    }
}