package com.certificados.app.controller;

import com.certificados.app.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/archivos")
public class FileStorageController {

    private final FileStorageService fileStorageService;

    public FileStorageController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/asociar")
    public ResponseEntity<Map<String, Object>> asociarArchivo(
            @RequestParam("documentoId") Long documentoId,
            @RequestParam("archivo") MultipartFile archivo) {

        String ruta = fileStorageService.almacenarArchivo(archivo);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("documentoId", documentoId);
        respuesta.put("nombreOriginal", archivo.getOriginalFilename());
        respuesta.put("ruta", ruta);
        respuesta.put("mensaje", "Archivo asociado correctamente al documento");

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/descargar/{nombreArchivo}")
    public ResponseEntity<Resource> consultarArchivoOriginal(@PathVariable String nombreArchivo) {
        Resource recurso = fileStorageService.cargarComoRecurso(nombreArchivo);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
                .body(recurso);
    }
}