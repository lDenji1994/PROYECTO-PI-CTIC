package com.certificados.app.service;

import com.certificados.app.model.Documento;
import com.certificados.app.repository.DocumentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final FileStorageService fileStorageService;

    public DocumentoService(DocumentoRepository documentoRepository, FileStorageService fileStorageService) {
        this.documentoRepository = documentoRepository;
        this.fileStorageService = fileStorageService;
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
}