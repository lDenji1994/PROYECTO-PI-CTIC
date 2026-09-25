package com.certificados.app.controller;

import com.certificados.app.dto.UsuarioResumenDTO;
import com.certificados.app.repository.UsuarioRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

/**
 * GET /api/usuarios -> usuarios activos (sin contrasenas ni correos).
 * Lo usa el panel para elegir el "usuario encargado" de una solicitud.
 * Solo lectura: la gestion de usuarios llegara con el modulo de login.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository repository;

    public UsuarioController(UsuarioRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<UsuarioResumenDTO> listarActivos() {
        return repository.findAll().stream()
                .filter(u -> !Boolean.FALSE.equals(u.getActivo()))
                .map(u -> new UsuarioResumenDTO(u.getId(), u.getNombreCompleto(), u.getUsuario(), u.getActivo()))
                .sorted(Comparator.comparing(UsuarioResumenDTO::nombreCompleto, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}
