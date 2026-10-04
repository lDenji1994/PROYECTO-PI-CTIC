package com.certificados.app.controller;

import com.certificados.app.dto.DatosCursoDTO;
import com.certificados.app.dto.VersionDocumentoDTO;
import com.certificados.app.service.VersionDocumentoService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
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

    /**
     * GET /api/versiones-documentos/{id}/archivo
     * Entrega el archivo original. Los PDF se abren en el navegador; Word y
     * Excel se descargan (nunca se ejecutan en el servidor).
     */
    @GetMapping("/{id}/archivo")
    public ResponseEntity<ByteArrayResource> descargar(
            @PathVariable Integer id) {

        byte[] archivo = service.obtenerArchivo(id);
        String nombreArchivo = service.obtenerNombreArchivo(id);

        ContentDisposition disposicion = (service.esPdf(nombreArchivo)
                ? ContentDisposition.inline()
                : ContentDisposition.attachment())
                // ContentDisposition escapa el nombre (tildes, comillas):
                // evita inyeccion de cabeceras.
                .filename(nombreArchivo, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(service.tipoMime(nombreArchivo)))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                // El navegador no debe "adivinar" otro tipo de contenido
                .header("X-Content-Type-Options", "nosniff")
                .contentLength(archivo.length)
                .body(new ByteArrayResource(archivo));
    }

    /* ------------------------------------------------------------------
       Datos del curso registrados a mano y correccion de cargas fallidas
       ------------------------------------------------------------------ */

    /** GET /api/versiones-documentos/{id}/datos -> datos del curso de esa version. */
    @GetMapping("/{id}/datos")
    public DatosCursoDTO obtenerDatos(@PathVariable Integer id) {
        return service.obtenerDatos(id);
    }

    /** PUT /api/versiones-documentos/{id}/datos -> corrige los datos del curso (JSON). */
    @PutMapping("/{id}/datos")
    public DatosCursoDTO actualizarDatos(
            @PathVariable Integer id,
            @RequestBody DatosCursoDTO datos) {
        return service.actualizarDatos(id, datos);
    }

    /**
     * POST /api/versiones-documentos/{id}/archivo  (multipart, campo "archivo")
     * Reemplaza el archivo de una version que se subio danado o equivocado.
     */
    @PostMapping(value = "/{id}/archivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> reemplazarArchivo(
            @PathVariable Integer id,
            @RequestParam("archivo") MultipartFile archivo) throws IOException {
        service.reemplazarArchivo(id, archivo);
        return ResponseEntity.noContent().build();
    }
}
