package com.certificados.app.controller;

import com.certificados.app.dto.PlanDTO;
import com.certificados.app.dto.PlanEstudioDTO;
import com.certificados.app.service.PlanEstudioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * PLANES DE ESTUDIO.
 *
 *   GET    /api/programas/{id}/planes                    planes del programa
 *   POST   /api/programas/{id}/planes                    { "nombre": "Plan 2026", "estado": "VIGENTE" }
 *   PUT    /api/planes/{idPlan}                          { "nombre": "...", "estado": "ANTIGUO" } (cualquiera de los dos)
 *   DELETE /api/planes/{idPlan}                          elimina el plan (las asignaturas no se borran)
 *
 *   GET    /api/planes/{idPlan}/asignaturas              asignaturas con su semestre
 *   PUT    /api/planes/{idPlan}/asignaturas/{idAsig}     { "semestre": 3 } (agrega o cambia)
 *   DELETE /api/planes/{idPlan}/asignaturas/{idAsig}     la quita del plan
 *
 * Permisos (SecurityConfig): cualquier usuario con sesion (auxiliar o
 * administrador) puede consultarlos y cambiarlos.
 */
@RestController
@RequestMapping("/api")
public class PlanEstudioController {

    private final PlanEstudioService service;

    public PlanEstudioController(PlanEstudioService service) {
        this.service = service;
    }

    @GetMapping("/programas/{idPrograma}/planes")
    public List<PlanDTO> listarPlanes(@PathVariable Integer idPrograma) {
        return service.listarPlanes(idPrograma);
    }

    @PostMapping("/programas/{idPrograma}/planes")
    public ResponseEntity<PlanDTO> crearPlan(@PathVariable Integer idPrograma,
                                             @RequestBody Map<String, Object> cuerpo) {
        Map<String, Object> datos = cuerpo == null ? Map.of() : cuerpo;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.crearPlan(idPrograma, texto(datos.get("nombre")), texto(datos.get("estado"))));
    }

    @PutMapping("/planes/{idPlan}")
    public PlanDTO actualizarPlan(@PathVariable Integer idPlan,
                                  @RequestBody Map<String, Object> cuerpo) {
        Map<String, Object> datos = cuerpo == null ? Map.of() : cuerpo;
        return service.actualizarPlan(idPlan, texto(datos.get("nombre")), texto(datos.get("estado")));
    }

    @DeleteMapping("/planes/{idPlan}")
    public ResponseEntity<Void> eliminarPlan(@PathVariable Integer idPlan) {
        service.eliminarPlan(idPlan);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/planes/{idPlan}/asignaturas")
    public List<PlanEstudioDTO> listar(@PathVariable Integer idPlan) {
        return service.listar(idPlan);
    }

    @PutMapping("/planes/{idPlan}/asignaturas/{idAsignatura}")
    public PlanEstudioDTO guardar(@PathVariable Integer idPlan,
                                  @PathVariable Integer idAsignatura,
                                  @RequestBody Map<String, Object> cuerpo) {
        Map<String, Object> datos = cuerpo == null ? Map.of() : cuerpo;
        return service.guardar(idPlan, idAsignatura, entero(datos.get("semestre")));
    }

    @DeleteMapping("/planes/{idPlan}/asignaturas/{idAsignatura}")
    public ResponseEntity<Void> quitar(@PathVariable Integer idPlan,
                                       @PathVariable Integer idAsignatura) {
        service.quitar(idPlan, idAsignatura);
        return ResponseEntity.noContent().build();
    }

    private static Integer entero(Object valor) {
        if (valor instanceof Number n) {
            return n.intValue();
        }
        if (valor instanceof String s && s.trim().matches("\\d{1,3}")) {
            return Integer.valueOf(s.trim());
        }
        return null;
    }

    private static String texto(Object valor) {
        return valor == null ? null : String.valueOf(valor);
    }
}
