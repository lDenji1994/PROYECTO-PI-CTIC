package com.certificados.app.service.impl;

import com.certificados.app.service.impl.AlmacenamientoServiceImpl;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.certificados.app.exception.AlmacenamientoException;
import com.certificados.app.model.Documento;
import com.certificados.app.service.AlmacenamientoService;

import jakarta.annotation.PostConstruct;

@Service
public class AlmacenamientoServiceImpl implements AlmacenamientoService {

    private final Path directorioRaiz;

    public AlmacenamientoServiceImpl(@Value("${app.almacenamiento.ruta:uploads}") String rutaAlmacenamiento) {
        this.directorioRaiz = Paths.get(rutaAlmacenamiento);
    }

    @PostConstruct
    public void inicializarEstructura() {
        try {
            if (!Files.exists(directorioRaiz)) {
                Files.createDirectories(directorioRaiz);
            }
        } catch (IOException e) {
            throw new AlmacenamientoException("No se pudo crear la carpeta raíz de almacenamiento", e);
        }
    }

    @Override
    public String guardar(MultipartFile archivo) {
        try {
            if (archivo == null || archivo.isEmpty()) {
                throw new AlmacenamientoException("El archivo no es válido");
            }

            String idUnico = UUID.randomUUID().toString();
            String nombreOriginal = archivo.getOriginalFilename();
            String nombreAlmacenado = idUnico + "_" + nombreOriginal;
            Path destino = directorioRaiz.resolve(nombreAlmacenado);

            try (InputStream contenido = archivo.getInputStream()) {
                Files.copy(contenido, destino, StandardCopyOption.REPLACE_EXISTING);
            }

            return nombreAlmacenado;
        } catch (IOException e) {
            throw new AlmacenamientoException("Error al guardar el archivo", e);
        }
    }

    public Documento guardarArchivo(InputStream contenido, String nombreOriginal, String tipoDocumento) {
        try {
            if (nombreOriginal == null || nombreOriginal.isEmpty()) {
                throw new AlmacenamientoException("El nombre del archivo no es válido");
            }

            String idUnico = UUID.randomUUID().toString();
            String nombreAlmacenado = idUnico + "_" + nombreOriginal;
            Path destino = directorioRaiz.resolve(nombreAlmacenado);

            Files.copy(contenido, destino, StandardCopyOption.REPLACE_EXISTING);
            long tamano = Files.size(destino);

            return new Documento(idUnico, nombreOriginal, destino.toString(), tipoDocumento, tamano, LocalDateTime.now());

        } catch (IOException e) {
            throw new AlmacenamientoException("Error al guardar el archivo: " + nombreOriginal, e);
        }
    }

    public Path cargarRuta(String nombreArchivo) {
        return directorioRaiz.resolve(nombreArchivo);
    }
}