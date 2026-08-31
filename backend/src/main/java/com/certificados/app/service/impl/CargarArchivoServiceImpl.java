package com.certificados.app.service.impl;

import com.certificados.app.dto.CargaArchivoResponseDTO;
import com.certificados.app.exception.AlmacenamientoException;
import com.certificados.app.model.Documento;
import com.certificados.app.repository.DocumentoRepository;
import com.certificados.app.service.AlmacenamientoService;
import com.certificados.app.service.CargarArchivoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CargarArchivoServiceImpl implements CargarArchivoService {

    private final AlmacenamientoService almacenamientoService;
    private final DocumentoRepository documentoRepository;

    private static final long LIMITE_TAMANO_BYTES = 10 * 1024 * 1024;

    public CargarArchivoServiceImpl(AlmacenamientoService almacenamientoService, DocumentoRepository documentoRepository) {
        this.almacenamientoService = almacenamientoService;
        this.documentoRepository = documentoRepository;
    }

    @Override
    @Transactional
    public CargaArchivoResponseDTO cargarArchivo(MultipartFile archivo, String tipoDocumento) {
        if (archivo == null || archivo.isEmpty()) {
            throw new AlmacenamientoException("No se puede cargar un archivo vacío.");
        }

        String nombreOriginal = archivo.getOriginalFilename();
        String contentType = archivo.getContentType();

        if (nombreOriginal == null || !nombreOriginal.toLowerCase().endsWith(".pdf") 
                || contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
            throw new AlmacenamientoException("Formato no permitido. Solo se aceptan archivos en formato PDF (.pdf).");
        }

        if (archivo.getSize() > LIMITE_TAMANO_BYTES) {
            throw new AlmacenamientoException("El archivo excede el tamaño máximo permitido de 10 MB.");
        }

        String nombreGuardado = almacenamientoService.guardar(archivo);

        Documento documento = new Documento(
                UUID.randomUUID().toString(),
                nombreOriginal,
                "uploads/" + nombreGuardado,
                tipoDocumento.toUpperCase(),
                archivo.getSize(),
                LocalDateTime.now()
        );

        Documento documentoGuardado = documentoRepository.save(documento);

        CargaArchivoResponseDTO response = new CargaArchivoResponseDTO();
        response.setId(documentoGuardado.getId());
        response.setNombreOriginal(documentoGuardado.getNombreOriginal());
        response.setTipoDocumento(documentoGuardado.getTipoDocumento());
        response.setTamanoBytes(documentoGuardado.getTamanoBytes());
        response.setMensaje("Archivo PDF cargado y registrado exitosamente.");
        response.setFechaCarga(documentoGuardado.getFechaCarga());

        return response;
    }
}