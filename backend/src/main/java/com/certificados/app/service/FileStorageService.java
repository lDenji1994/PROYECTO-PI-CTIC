package com.certificados.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageLocation;

    public FileStorageService(
            @Value("${app.storage.upload-dir}") String uploadDir) {

        this.storageLocation = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(this.storageLocation);
        } catch (IOException e) {
            throw new RuntimeException(
                    "No se pudo crear el directorio de almacenamiento", e
            );
        }
    }

    public String almacenarArchivo(MultipartFile archivo) {

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "El archivo no puede estar vacío"
            );
        }

        String nombreOriginal = archivo.getOriginalFilename();

        String extension = "";

        if (nombreOriginal != null && nombreOriginal.contains(".")) {
            extension = nombreOriginal.substring(
                    nombreOriginal.lastIndexOf(".")
            );
        }

        String nombreArchivo = UUID.randomUUID() + extension;

        Path destino = storageLocation.resolve(nombreArchivo).normalize();

        if (!destino.getParent().equals(storageLocation)) {
            throw new RuntimeException(
                    "La ruta del archivo no es válida"
            );
        }

        try (InputStream inputStream = archivo.getInputStream()) {

            Files.copy(
                    inputStream,
                    destino,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return destino.toString();

        } catch (IOException e) {
            throw new RuntimeException(
                    "No se pudo almacenar el archivo", e
            );
        }
    }
}