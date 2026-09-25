package com.certificados.app.controller;

import com.certificados.app.dto.DocumentoResumenDTO;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import com.certificados.app.dto.DocumentoCertificadoDTO;
import com.certificados.app.model.DocumentoAcademico;
import com.certificados.app.service.DocumentoAcademicoService;
import com.certificados.app.service.DocumentoCertificadoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documentos-academicos")
public class DocumentoAcademicoController {

    private final DocumentoAcademicoService service;
    private final DocumentoCertificadoService documentoCertificadoService;

    public DocumentoAcademicoController(
            DocumentoAcademicoService service,
            DocumentoCertificadoService documentoCertificadoService) {

        this.service = service;
        this.documentoCertificadoService =
                documentoCertificadoService;
    }

    @GetMapping
    public List<DocumentoAcademico> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public DocumentoAcademico buscarPorId(
            @PathVariable Integer id) {

        return service.buscarPorId(id);
    }

    @GetMapping("/asignatura/{idAsignatura}")
    public List<DocumentoAcademico> listarPorAsignatura(
            @PathVariable Integer idAsignatura) {

        return service.listarPorAsignatura(idAsignatura);
    }

    @GetMapping("/solicitud/{idSolicitud}")
    public List<DocumentoCertificadoDTO> listarPorSolicitud(
            @PathVariable Integer idSolicitud) {

        return documentoCertificadoService
                .obtenerDocumentosPorSolicitud(idSolicitud);
    }

    @PostMapping
    public ResponseEntity<DocumentoAcademico> guardar(
            @Valid @RequestBody DocumentoAcademico documento) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.guardar(documento));
    }

    /* ------------------------------------------------------------------
       Endpoints usados por el panel (Carga de informacion / Cursos)
       ------------------------------------------------------------------ */

    /**
     * GET /api/documentos-academicos/resumen[?idAsignatura=5]
     * Documentos con asignatura (materia + curso), tipo, vigencia del
     * formato y versiones cargadas (sin el PDF), mas recientes primero.
     */
    @GetMapping("/resumen")
    public List<DocumentoResumenDTO> resumen(
            @RequestParam(required = false) Integer idAsignatura) {
        return service.listarResumen(idAsignatura);
    }

    /**
     * POST /api/documentos-academicos/cargar  (multipart/form-data)
     * Registra (o reutiliza) el documento de la asignatura + tipo y guarda
     * el PDF como nueva version, todo en una sola operacion.
     */
    @PostMapping(value = "/cargar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoResumenDTO> cargar(
            @RequestParam("idAsignatura") Integer idAsignatura,
            @RequestParam("idTipoDocumentoAcademico") Integer idTipoDocumentoAcademico,
            @RequestParam("codigoFormato") String codigoFormato,
            @RequestParam("versionFormato") String versionFormato,
            @RequestParam(value = "periodo", required = false) String periodo,
            @RequestParam(value = "idPrograma", required = false) Integer idPrograma,
            @RequestParam("archivo") MultipartFile archivo) throws IOException {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                service.cargarDocumento(idAsignatura, idTipoDocumentoAcademico,
                        codigoFormato, versionFormato, periodo, idPrograma, archivo));
    }
}
