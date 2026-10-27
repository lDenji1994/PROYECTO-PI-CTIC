package com.certificados.app.service;

import com.certificados.app.exception.AlmacenamientoException;
import com.certificados.app.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

/**
 * ARCHIVOS FUERA DE LA BASE DE DATOS.
 *
 * Los documentos cargados (cartas descriptivas, syllabus...) y los PDF de
 * los certificados se guardan en una CARPETA (local o compartida); la base
 * de datos solo guarda la ruta relativa del archivo, por ejemplo:
 *
 *     documentos/2026-2/3f2a9c1e-....pdf
 *     certificados/2026/8b7d0c44-....pdf
 *
 * Asi la base no crece con cada archivo y los respaldos son livianos.
 *
 * SE PUEDE MODIFICAR (en application-local.properties de cada PC o en el
 * archivo de configuracion del servidor):
 *     app.almacenamiento.ruta=C:/Users/brein/OneDrive/Desktop/demo
 *     app.almacenamiento.ruta=//servidor-ctic/certificados      (carpeta compartida)
 * Si no se configura, se usa la carpeta "certificados-archivos" dentro de la
 * carpeta personal del usuario que ejecuta el servidor.
 *
 * NO MODIFICAR: la validacion de que la ruta quede DENTRO de la carpeta
 * configurada (evita que una ruta manipulada lea archivos del sistema).
 */
@Service
public class AlmacenamientoArchivosService {

    private static final Logger LOG = LoggerFactory.getLogger(AlmacenamientoArchivosService.class);

    public static final String DOCUMENTOS = "documentos";
    public static final String CERTIFICADOS = "certificados";

    private final Path raiz;

    public AlmacenamientoArchivosService(
            @Value("${app.almacenamiento.ruta:${user.home}/certificados-archivos}") String ruta) {
        this.raiz = Paths.get(ruta).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.raiz);
        } catch (IOException e) {
            throw new AlmacenamientoException("No se pudo crear la carpeta de archivos " + this.raiz
                    + ". Revisa app.almacenamiento.ruta y los permisos de la carpeta.", e);
        }
        LOG.info("ARCHIVOS: documentos y certificados se guardan en {}", this.raiz);
    }

    /** Carpeta donde se guardan los archivos (para mostrarla en los mensajes). */
    public Path raiz() {
        return raiz;
    }

    /** Guarda un documento cargado, en documentos/{periodo}/. Devuelve la ruta relativa. */
    public String guardarDocumento(String periodo, String nombreOriginal, byte[] contenido) {
        String carpeta = periodo == null || periodo.isBlank() ? "sin-periodo" : periodo.trim();
        return guardar(DOCUMENTOS + "/" + carpeta, nombreOriginal, contenido);
    }

    /** Guarda el PDF de un certificado, en certificados/{año}/. Devuelve la ruta relativa. */
    public String guardarCertificado(String nombreOriginal, byte[] contenido) {
        return guardar(CERTIFICADOS + "/" + LocalDate.now().getYear(), nombreOriginal, contenido);
    }

    /**
     * Escribe el archivo con un nombre unico (no se pisan archivos con el
     * mismo nombre). Si la transaccion de la base de datos falla despues,
     * el archivo se borra para no dejar archivos sueltos.
     */
    public String guardar(String subcarpeta, String nombreOriginal, byte[] contenido) {
        String relativa = subcarpeta.replaceAll("[^A-Za-z0-9_\\-/]", "_") + "/"
                + UUID.randomUUID() + extension(nombreOriginal);
        Path destino = resolver(relativa);
        try {
            Files.createDirectories(destino.getParent());
            // Se escribe primero en un temporal y luego se mueve: nunca queda un archivo a medias
            Path temporal = Files.createTempFile(destino.getParent(), "subiendo-", ".tmp");
            Files.write(temporal, contenido);
            try {
                Files.move(temporal, destino, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new AlmacenamientoException("No se pudo guardar el archivo en " + raiz
                    + ". Revisa que la carpeta exista y tenga permisos de escritura.", e);
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int estado) {
                    if (estado == STATUS_ROLLED_BACK) {
                        borrar(relativa);
                    }
                }
            });
        }
        return relativa;
    }

    /** Lee un archivo guardado. */
    public byte[] leer(String relativa) {
        if (relativa == null || relativa.isBlank()) {
            throw new ResourceNotFoundException("El registro no tiene archivo asociado.");
        }
        Path archivo = resolver(relativa);
        if (!Files.isRegularFile(archivo)) {
            throw new ResourceNotFoundException("No se encontró el archivo " + relativa
                    + " en la carpeta de archivos (" + raiz + "). ¿Se movió o se borró la carpeta?");
        }
        try {
            return Files.readAllBytes(archivo);
        } catch (IOException e) {
            throw new AlmacenamientoException("No se pudo leer el archivo " + relativa + ".", e);
        }
    }

    /**
     * Borra el archivo cuando la transaccion se confirme (si se cancela, el
     * archivo se conserva porque el registro sigue existiendo).
     */
    public void borrarAlConfirmar(String relativa) {
        if (relativa == null || relativa.isBlank()) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    borrar(relativa);
                }
            });
        } else {
            borrar(relativa);
        }
    }

    private void borrar(String relativa) {
        try {
            Files.deleteIfExists(resolver(relativa));
        } catch (IOException | RuntimeException e) {
            LOG.warn("ARCHIVOS: no se pudo borrar {} ({})", relativa, e.getMessage());
        }
    }

    /** Ruta absoluta de un archivo, comprobando que quede dentro de la carpeta configurada. */
    private Path resolver(String relativa) {
        Path archivo = raiz.resolve(relativa.replace('\\', '/')).normalize();
        if (!archivo.startsWith(raiz)) {
            throw new AlmacenamientoException("Ruta de archivo no permitida: " + relativa, null);
        }
        return archivo;
    }

    /** ".pdf", ".xlsm"... (solo letras y numeros, en minuscula). */
    private static String extension(String nombre) {
        if (nombre == null) {
            return "";
        }
        int punto = nombre.lastIndexOf('.');
        if (punto < 0 || punto == nombre.length() - 1) {
            return "";
        }
        String ext = nombre.substring(punto + 1).toLowerCase(Locale.ROOT);
        return ext.matches("[a-z0-9]{1,8}") ? "." + ext : "";
    }
}
