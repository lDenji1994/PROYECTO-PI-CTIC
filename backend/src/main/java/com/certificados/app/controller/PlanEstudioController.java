package com.certificados.app.controller;

import com.certificados.app.dto.PlanEstudioDTO;
import com.certificados.app.service.PlanEstudioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * PLAN DE ESTUDIOS DE UN PROGRAMA.
 *
 *   GET    /api/programas/{id}/plan                    asignaturas con su semestre
 *   PUT    /api/programas/{id}/plan/{idAsignatura}     cuerpo { "semestre": 3 } (agrega o cambia)
 *   DELETE /api/programas/{id}/plan/{idAsignatura}     la quita del plan
 *
 * Permisos (SecurityConfig): cualquier usuario con sesion (auxiliar o
 * administrador) puede consultarlo y cambiarlo.
 */
@RestController
@RequestMapping("/api/programas/{idPrograma}/plan")
public class PlanEstudioController {

    private final PlanEstudioService service;

    public PlanEstudioController(PlanEstudioService service) {
        this.service = service;
    }

    @GetMapping
    public List<PlanEstudioDTO> listar(@PathVariable Integer idPrograma) {
        return service.listar(idPrograma);
    }

    @PutMapping("/{idAsignatura}")
    public PlanEstudioDTO guardar(@PathVariable Integer idPrograma,
                                  @PathVariable Integer idAsignatura,
                                  @RequestBody Map<String, Integer> cuerpo) {
        return service.guardar(idPrograma, idAsignatura, cuerpo == null ? null : cuerpo.get("semestre"));
    }

    @DeleteMapping("/{idAsignatura}")
    public ResponseEntity<Void> quitar(@PathVariable Integer idPrograma,
                                       @PathVariable Integer idAsignatura) {
        service.quitar(idPrograma, idAsignatura);
        return ResponseEntity.noContent().build();
    }
}
