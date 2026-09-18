package com.certificados.app.controller;

import com.certificados.app.model.Documento;
import com.certificados.app.service.DocumentoService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documentos")
@CrossOrigin(origins = "http://localhost:4200")
public class DocumentoController {

    private final DocumentoService documentoService;

    public DocumentoController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<Documento> registrarDocumento(@RequestParam("archivo") MultipartFile archivo) {
        Documento documentoGuardado = documentoService.registrarDocumento(archivo);
        return new ResponseEntity<>(documentoGuardado, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Documento>> listarDocumentos(
            @RequestParam(required = false) String tipoDocumento) {
        return ResponseEntity.ok(documentoService.buscarConFiltros(tipoDocumento));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Documento> obtenerPorId(@PathVariable String id) {
        Documento documento = documentoService.obtenerPorId(id);
        return ResponseEntity.ok(documento);
    }

    @GetMapping("/descargar/{id}")
    public ResponseEntity<Resource> descargarDocumento(@PathVariable String id) {
        Documento documento = documentoService.obtenerPorId(id);
        Resource recurso = documentoService.cargarComoRecurso(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(documento.getTipoDocumento()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + documento.getNombreOriginal() + "\"")
                .body(recurso);
    }

    @GetMapping("/ver/{id}")
    public ResponseEntity<Resource> verDocumentoEnLinea(@PathVariable String id) {
        Documento documento = documentoService.obtenerPorId(id);
        Resource recurso = documentoService.cargarComoRecurso(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(documento.getTipoDocumento()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + documento.getNombreOriginal() + "\"")
                .body(recurso);
    }
}