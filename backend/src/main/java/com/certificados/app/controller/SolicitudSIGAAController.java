package com.certificados.app.controller;

import com.certificados.app.dto.SolicitudSIGAADTO;
import com.certificados.app.service.SolicitudSIGAAService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudSIGAAController {

    private final SolicitudSIGAAService solicitudSIGAAService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitudSIGAADTO recibirSolicitud(
        @Valid @RequestBody SolicitudSIGAADTO solicitud) {

        return solicitudSIGAAService.recibirSolicitud(solicitud);
    }

    @GetMapping
    public List<SolicitudSIGAADTO> listarSolicitudes() {
        return solicitudSIGAAService.listarSolicitudes();
    }

    @GetMapping("/{solicitudId}")
    public SolicitudSIGAADTO buscarPorSolicitudId(
        @PathVariable String solicitudId) {

        return solicitudSIGAAService.buscarPorSolicitudId(solicitudId);
    }

}