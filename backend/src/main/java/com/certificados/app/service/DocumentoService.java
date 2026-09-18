package com.certificados.app.service;

import com.certificados.app.model.Documento;
import com.certificados.app.repository.DocumentoRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final FileStorageService fileStorageService;

    public DocumentoService(DocumentoRepository documentoRepository, FileStorageService fileStorageService) {
        this.documentoRepository = documentoRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<Documento> listarTodos() {
        return documentoRepository.findAll();
    }

    public List<Documento> buscarConFiltros(String tipoDocumento) {
        if (tipoDocumento != null && !tipoDocumento.isBlank()) {
            return documentoRepository.findByTipoDocumento(tipoDocumento);
        }
        return documentoRepository.findAll();
    }

    public Documento registrarDocumento(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        String ruta = fileStorageService.almacenarArchivo(archivo);
        String id = UUID.randomUUID().toString();

        Documento documento = new Documento(
                id,
                archivo.getOriginalFilename(),
                ruta,
                archivo.getContentType(),
                archivo.getSize(),
                LocalDateTime.now()
        );

        return documentoRepository.save(documento);
    }

    public Documento obtenerPorId(String id) {
        return documentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado con el ID: " + id));
    }

    public Resource cargarComoRecurso(String id) {
        Documento documento = obtenerPorId(id);
        try {
            Path rutaArchivo = Paths.get(documento.getRutaAlmacenamiento());
            Resource recurso = new UrlResource(rutaArchivo.toUri());
            if (recurso.exists() || recurso.isReadable()) {
                return recurso;
            } else {
                throw new RuntimeException("No se pudo leer el archivo en la ruta especificada");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error al construir la ruta del archivo: " + e.getMessage());
        }
    }
}