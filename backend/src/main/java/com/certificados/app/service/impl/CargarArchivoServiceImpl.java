package com.certificados.app.service.impl;

import com.certificados.app.dto.CargaArchivoResponseDTO;
import com.certificados.app.exception.AlmacenamientoException;
import com.certificados.app.model.Documento;
import com.certificados.app.repository.DocumentoRepository;
import com.certificados.app.service.AlmacenamientoService;
import com.certificados.app.service.CargarArchivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CargarArchivoServiceImpl implements CargarArchivoService {

    private final AlmacenamientoService almacenamientoService;
    private final DocumentoRepository documentoRepository;

    private static final long LIMITE_TAMANO_BYTES = 10 * 1024 * 1024; // 10 MB

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

        Documento documento = Documento.builder()
                .id(UUID.randomUUID().toString())
                .nombreOriginal(nombreOriginal)
                .rutaAlmacenamiento("uploads/" + nombreGuardado)
                .tipoDocumento(tipoDocumento.toUpperCase())
                .tamanoBytes(archivo.getSize())
                .fechaCarga(LocalDateTime.now())
                .build();

        Documento documentoGuardado = documentoRepository.save(documento);

        return CargaArchivoResponseDTO.builder()
                .id(documentoGuardado.getId())
                .nombreOriginal(documentoGuardado.getNombreOriginal())
                .tipoDocumento(documentoGuardado.getTipoDocumento())
                .tamanoBytes(documentoGuardado.getTamanoBytes())
                .mensaje("Archivo PDF cargado y registrado exitosamente.")
                .fechaCarga(documentoGuardado.getFechaCarga())
                .build();
    }
}