package com.certificados.app.controller;

import com.certificados.app.service.EliminacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ELIMINAR DESDE EL PANEL (botones "Eliminar").
 *
 *   DELETE /api/versiones-documentos/{id}   documento cargado (archivo + datos del curso)
 *   DELETE /api/asignaturas/{id}            asignatura sin documentos ni solicitudes
 *   DELETE /api/programas/{id}              programa que ningun documento usa   (solo administrador)
 *   DELETE /api/solicitudes-certificados/{id}  solicitud + asignaturas + certificado
 *   DELETE /api/usuarios/{id}               cuenta sin actividad                (solo administrador)
 *
 * Responden 204 (sin contenido) si se elimino. Las reglas estan en
 * EliminacionService. Quien puede usarlos se decide en SecurityConfig.
 *
 * Seguridad: el id llega como numero en la URL (Spring rechaza cualquier
 * otra cosa) y las consultas son parametrizadas; no hay SQL armado a mano.
 */
@RestController
@RequestMapping("/api")
public class EliminacionController {

    private final EliminacionService eliminacionService;

    public EliminacionController(EliminacionService eliminacionService) {
        this.eliminacionService = eliminacionService;
    }

    @DeleteMapping("/versiones-documentos/{id}")
    public ResponseEntity<Void> eliminarVersion(@PathVariable Integer id) {
        eliminacionService.eliminarVersion(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/asignaturas/{id}")
    public ResponseEntity<Void> eliminarAsignatura(@PathVariable Integer id) {
        eliminacionService.eliminarAsignatura(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/programas/{id}")
    public ResponseEntity<Void> eliminarPrograma(@PathVariable Integer id) {
        eliminacionService.eliminarPrograma(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/solicitudes-certificados/{id}")
    public ResponseEntity<Void> eliminarSolicitud(@PathVariable Integer id) {
        eliminacionService.eliminarSolicitud(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Integer id) {
        eliminacionService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
