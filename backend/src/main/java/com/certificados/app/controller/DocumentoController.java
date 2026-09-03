package com.certificados.app.controller;

import com.certificados.app.model.Documento;
import com.certificados.app.service.DocumentoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documentos")
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
}