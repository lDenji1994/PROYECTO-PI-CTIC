package com.certificados.app.controller;

import com.certificados.app.dto.VersionDocumentoDTO;
import com.certificados.app.service.VersionDocumentoService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/versiones-documentos")
public class VersionDocumentoController {

    private final VersionDocumentoService service;

    public VersionDocumentoController(
            VersionDocumentoService service) {
        this.service = service;
    }

    @GetMapping
    public List<VersionDocumentoDTO> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public VersionDocumentoDTO buscarPorId(
            @PathVariable Integer id) {

        return service.buscarPorId(id);
    }

    @GetMapping("/documento/{idDocumentoAcademico}")
    public List<VersionDocumentoDTO> listarPorDocumento(
            @PathVariable Integer idDocumentoAcademico) {

        return service.listarPorDocumento(
                idDocumentoAcademico
        );
    }

    @PostMapping(
            value = "/cargar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Void> cargar(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("idDocumentoAcademico") Integer idDocumentoAcademico,
            @RequestParam(value = "periodo", required = false) String periodo
    ) throws IOException {

        service.cargarArchivo(
                archivo,
                idDocumentoAcademico,
                periodo
        );

        return ResponseEntity.status(201).build();
    }

    @GetMapping("/{id}/archivo")
    public ResponseEntity<ByteArrayResource> descargar(
            @PathVariable Integer id) {

        byte[] archivo = service.obtenerArchivo(id);
        String nombreArchivo = service.obtenerNombreArchivo(id);

        ByteArrayResource resource =
                new ByteArrayResource(archivo);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" +
                                nombreArchivo +
                                "\""
                )
                .contentLength(archivo.length)
                .body(resource);
    }
}
