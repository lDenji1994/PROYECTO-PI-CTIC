package com.certificados.app.controller;

import com.certificados.app.dto.AsignaturaDTO;
import com.certificados.app.service.AsignaturaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Asignaturas.
 *   GET  /api/asignaturas                -> lista (con codigoMateria / codigoCurso separados)
 *   GET  /api/asignaturas/{id}
 *   GET  /api/asignaturas/codigo/{codigo} -> acepta "FION 0001", "FION-0001" o "FION0001"
 *   POST /api/asignaturas   { "codigoMateria":"FION", "codigoCurso":"0001", "nombre":"Ondas" }
 *        (tambien acepta el formato anterior { "codigo":"FION 0001", "nombre":"Ondas" })
 */
@RestController
@RequestMapping("/api/asignaturas")
public class AsignaturaController {

    private final AsignaturaService service;

    public AsignaturaController(AsignaturaService service) {
        this.service = service;
    }

    @GetMapping
    public List<AsignaturaDTO> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id}")
    public AsignaturaDTO buscarPorId(@PathVariable Integer id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/codigo/{codigo}")
    public AsignaturaDTO buscarPorCodigo(@PathVariable String codigo) {
        return service.buscarPorCodigo(codigo);
    }

    @PostMapping
    public ResponseEntity<AsignaturaDTO> crear(@Valid @RequestBody AsignaturaDTO asignatura) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(asignatura));
    }
}
