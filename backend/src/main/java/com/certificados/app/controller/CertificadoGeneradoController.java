package com.certificados.app.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;

import com.certificados.app.exception.ResourceNotFoundException;
import com.certificados.app.model.CertificadoGenerado;
import com.certificados.app.service.CertificadoGeneradorService;
import com.certificados.app.repository.CertificadoGeneradoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificados-generados")
@CrossOrigin(origins = "http://localhost:4200")
public class CertificadoGeneradoController {

    private final CertificadoGeneradorService service;
    private final CertificadoGeneradoRepository repository;

    public CertificadoGeneradoController(
            CertificadoGeneradorService service,
            CertificadoGeneradoRepository repository) {

        this.service = service;
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<CertificadoGenerado>> listarTodos() {
        return ResponseEntity.ok(
                repository.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertificadoGenerado> buscarPorId(
            @PathVariable Integer id) {

        CertificadoGenerado certificado =
                repository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Certificado generado no encontrado con id "
                                                + id
                                ));

        return ResponseEntity.ok(certificado);
    }

    @GetMapping("/solicitud/{idSolicitud}")
    public ResponseEntity<CertificadoGenerado> buscarPorSolicitud(
            @PathVariable Integer idSolicitud) {

        CertificadoGenerado certificado =
                repository
                        .findByIdSolicitudCertificado(idSolicitud)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No existe un certificado generado para la solicitud "
                                                + idSolicitud
                                ));

        return ResponseEntity.ok(certificado);
    }

    @PostMapping("/solicitud/{idSolicitud}")
    public ResponseEntity<CertificadoGenerado> generar(
            @PathVariable Integer idSolicitud,
            @RequestParam Integer idVersionPlantilla) {

        return ResponseEntity.ok(
                service.generar(
                        idSolicitud,
                        idVersionPlantilla
                )
        );
    }

    /**
     * GET /api/certificados-generados/solicitud/{idSolicitud}/archivo
     * Descarga/abre el PDF del certificado generado para una solicitud.
     * (Agregado para el boton "Ver PDF" del panel de Certificaciones.)
     */
    @GetMapping("/solicitud/{idSolicitud}/archivo")
    public ResponseEntity<ByteArrayResource> descargarPorSolicitud(
            @PathVariable Integer idSolicitud) {

        CertificadoGenerado certificado = repository
                .findByIdSolicitudCertificado(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un certificado generado para la solicitud " + idSolicitud));

        byte[] archivo = certificado.getArchivo();
        String nombre = certificado.getNombreArchivo() == null
                ? "certificado-" + idSolicitud + ".pdf"
                : certificado.getNombreArchivo();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(nombre, StandardCharsets.UTF_8).build().toString())
                .contentLength(archivo.length)
                .body(new ByteArrayResource(archivo));
    }
}
