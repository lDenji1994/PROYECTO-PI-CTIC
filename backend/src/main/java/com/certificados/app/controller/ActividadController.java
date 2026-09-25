package com.certificados.app.controller;

import com.certificados.app.dto.ActividadDTO;
import com.certificados.app.service.ActividadService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Actividad reciente del Dashboard (lectura de la bitacora ya "traducida").
 *
 *   GET /api/actividades?limite=20                      -> todas
 *   GET /api/actividades?modulo=documentos&limite=50    -> solo un modulo
 *
 * Modulos validos: documentos | cursos | certificaciones.
 * La bitacora "cruda" sigue disponible en /api/logs (LogController).
 */
@RestController
@RequestMapping("/api/actividades")
public class ActividadController {

    private final ActividadService service;

    public ActividadController(ActividadService service) {
        this.service = service;
    }

    @GetMapping
    public List<ActividadDTO> listar(
            @RequestParam(required = false) String modulo,
            @RequestParam(defaultValue = "20") int limite) {
        return service.listarRecientes(modulo, limite);
    }
}
