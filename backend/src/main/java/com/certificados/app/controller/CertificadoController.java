package com.certificados.app.controller;

import com.certificados.app.dto.ApiResponse;
import com.certificados.app.dto.CertificadoDTO;
import com.certificados.app.dto.HistorialEstadoCertificadoDTO;
import com.certificados.app.model.EstadoCertificado;
import com.certificados.app.service.CertificadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificados")
public class CertificadoController {

    private final CertificadoService certificadoService;

    public CertificadoController(CertificadoService certificadoService) {
        this.certificadoService = certificadoService;
    }
    @GetMapping
    public List<CertificadoDTO> listar() {
        return certificadoService.listarTodos();
    }

    @GetMapping("/estado/{estado}")
    public List<CertificadoDTO> listarPorEstado(@PathVariable EstadoCertificado estado) {
        return certificadoService.listarPorEstado(estado);
    }

    @GetMapping("/estudiante/{estudianteId}")
    public List<CertificadoDTO> listarPorEstudiante(@PathVariable("estudianteId") Long estudianteId) {
        return certificadoService.listarPorEstudiante(estudianteId);
    }

    @GetMapping("/{id}")
    public CertificadoDTO buscarPorId(@PathVariable("id") Long id) {
        return certificadoService.buscarPorId(id);
    }

    @GetMapping("/{id}/historial")
    public List<HistorialEstadoCertificadoDTO> historial(@PathVariable("id") Long id) {
        return certificadoService.listarHistorial(id);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CertificadoDTO>> solicitar(@Valid @RequestBody CertificadoDTO dto) {
        CertificadoDTO creado = certificadoService.solicitar(dto);
        ApiResponse<CertificadoDTO> response = new ApiResponse<>(
                true,
                "Certificado solicitado exitosamente",
            creado
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/emitir")
    public CertificadoDTO emitir(@PathVariable("id") Long id) {
        return certificadoService.emitir(id);
    }

    @PatchMapping("/{id}/anular")
    public CertificadoDTO anular(@PathVariable("id") Long id) {
        return certificadoService.anular(id);
    }
}