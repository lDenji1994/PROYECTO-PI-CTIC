package com.certificados.app.controller;

import com.certificados.app.dto.AsignaturaDTO;
import com.certificados.app.service.AsignaturaService;

import com.certificados.app.dto.SolicitudCertificadoDTO;
import com.certificados.app.model.Asignatura;
import com.certificados.app.model.EstadoSolicitudCertificado;
import com.certificados.app.service.SolicitudCertificadoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes-certificados")
@CrossOrigin(origins = "http://localhost:4200")
public class SolicitudCertificadoController {

    private final SolicitudCertificadoService service;

    public SolicitudCertificadoController(
            SolicitudCertificadoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<SolicitudCertificadoDTO>> listarTodos() {
        return ResponseEntity.ok(
                service.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudCertificadoDTO> buscarPorId(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                service.buscarPorId(id)
        );
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<SolicitudCertificadoDTO>> listarPorEstado(
            @PathVariable EstadoSolicitudCertificado estado) {

        return ResponseEntity.ok(
                service.listarPorEstado(estado)
        );
    }

    @GetMapping("/estudiante/{idEstudiante}")
    public ResponseEntity<List<SolicitudCertificadoDTO>> listarPorEstudiante(
            @PathVariable Integer idEstudiante) {

        return ResponseEntity.ok(
                service.listarPorEstudiante(idEstudiante)
        );
    }

    @PostMapping
    public ResponseEntity<SolicitudCertificadoDTO> crear(
            @RequestBody SolicitudCertificadoDTO dto) {

        return ResponseEntity.ok(
                service.crear(dto)
        );
    }

    @PatchMapping("/{id}/procesar")
    public ResponseEntity<SolicitudCertificadoDTO> iniciarProcesamiento(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                service.iniciarProcesamiento(id)
        );
    }

    @PatchMapping("/{id}/esperar-documentos")
    public ResponseEntity<SolicitudCertificadoDTO> esperarDocumentos(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                service.esperarDocumentos(id)
        );
    }

    @PatchMapping("/{id}/realizar")
    public ResponseEntity<SolicitudCertificadoDTO> marcarRealizada(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                service.marcarRealizada(id)
        );
    }

    @PatchMapping("/{id}/error")
    public ResponseEntity<SolicitudCertificadoDTO> marcarError(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                service.marcarError(id)
        );
    }

    @PostMapping("/{id}/asignaturas/{idAsignatura}")
    public ResponseEntity<Void> agregarAsignatura(
            @PathVariable Integer id,
            @PathVariable Integer idAsignatura) {

        service.agregarAsignatura(
                id,
                idAsignatura
        );

        return ResponseEntity.ok().build();
    }

    /** Asignaturas de la solicitud, con codigo de materia y de curso separados. */
    @GetMapping("/{id}/asignaturas")
    public ResponseEntity<List<AsignaturaDTO>> listarAsignaturas(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                service.listarAsignaturas(id).stream()
                        .map(AsignaturaService::aDTO)
                        .toList()
        );
    }
}
