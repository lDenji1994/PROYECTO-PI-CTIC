package com.certificados.app.controller;

import com.certificados.app.model.Log;
import com.certificados.app.service.LogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogService service;

    public LogController(LogService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Log>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Log> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Log>> listarPorUsuario(
            @PathVariable Integer idUsuario) {

        return ResponseEntity.ok(
                service.listarPorUsuario(idUsuario)
        );
    }

    @GetMapping("/tabla/{nombreTabla}")
    public ResponseEntity<List<Log>> listarPorTabla(
            @PathVariable String nombreTabla) {

        return ResponseEntity.ok(
                service.listarPorTabla(nombreTabla)
        );
    }

    @GetMapping("/proceso/{nombreProceso}")
    public ResponseEntity<List<Log>> listarPorProceso(
            @PathVariable String nombreProceso) {

        return ResponseEntity.ok(
                service.listarPorProceso(nombreProceso)
        );
    }

    @PostMapping
    public ResponseEntity<Log> iniciar(
            @RequestParam String ip,
            @RequestParam String nombreTabla,
            @RequestParam String nombreProceso,
            @RequestParam Integer idUsuario) {

        return ResponseEntity.ok(
                service.iniciar(
                        ip,
                        nombreTabla,
                        nombreProceso,
                        idUsuario
                )
        );
    }

    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<Log> finalizar(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                service.finalizar(id)
        );
    }
}