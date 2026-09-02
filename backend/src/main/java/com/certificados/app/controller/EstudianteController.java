package com.certificados.app.controller;

import com.certificados.app.dto.EstudianteDTO;
import com.certificados.app.service.EstudianteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estudiantes")

public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }
    
    @GetMapping
    public List<EstudianteDTO> listar() {
        return estudianteService.listarTodos();
    }

    @GetMapping("/{id}")
    public EstudianteDTO buscarPorId(@PathVariable Long id) {
        return estudianteService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EstudianteDTO crear(@Valid @RequestBody EstudianteDTO dto) {
        return estudianteService.crear(dto);
    }

    @PutMapping("/{id}")
    public EstudianteDTO actualizar(@PathVariable Long id, @Valid @RequestBody EstudianteDTO dto) {
        return estudianteService.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        estudianteService.eliminar(id);
    }
}
