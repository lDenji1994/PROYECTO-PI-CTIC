package com.certificados.app.controller;

import com.certificados.app.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/archivos")
@RequiredArgsConstructor
public class FileStorageController {

    private final FileStorageService fileStorageService;

    @PostMapping("/cargar")
    public ResponseEntity<Map<String, String>> cargarArchivo(
            @RequestParam("archivo") MultipartFile archivo) {

        String ruta = fileStorageService.almacenarArchivo(archivo);

        Map<String, String> respuesta = new HashMap<>();

        respuesta.put("mensaje", "Archivo almacenado correctamente");
        respuesta.put("ruta", ruta);

        return ResponseEntity.ok(respuesta);
    }
}