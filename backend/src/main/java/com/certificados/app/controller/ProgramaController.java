package com.certificados.app.controller;

import com.certificados.app.dto.ProgramaDTO;
import com.certificados.app.service.ProgramaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/programas")
public class ProgramaController {

    private final ProgramaService programaService;

    public ProgramaController(ProgramaService programaService) {
        this.programaService = programaService;
    }

    @GetMapping
    public ResponseEntity<List<ProgramaDTO>> listar() {
        return ResponseEntity.ok(programaService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProgramaDTO> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(programaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody ProgramaDTO dto) {
        ProgramaDTO creado = programaService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "success", true,
                        "message", "Programa registrado exitosamente",
                        "data", creado
                ));
    }
}
