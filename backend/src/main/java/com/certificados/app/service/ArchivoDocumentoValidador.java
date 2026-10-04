package com.certificados.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * QUE ARCHIVOS SE PUEDEN CARGAR (PDF, Word y Excel) Y COMO SE VALIDAN.
 *
 * Los formatos institucionales llegan en distintos tipos de archivo:
 * la carta descriptiva vigente viene en Excel (.xlsm / .xlsx), algunos
 * syllabus en Word (.docx / .doc) y otros en PDF.
 *
 * Seguridad: NO se confia en la extension ni en el tipo que dice el
 * navegador. Se revisa la "firma" (los primeros bytes) del contenido:
 *
 *     PDF                 -> %PDF
 *     docx, xlsx, xlsm    -> PK..   (son archivos ZIP por dentro)
 *     doc, xls (antiguos) -> D0 CF 11 E0
 *
 * El archivo se guarda tal cual: nunca se abre ni se ejecuta en el
 * servidor (importante para los .xlsm, que traen macros).
 *
 * SE PUEDE MODIFICAR: la lista de extensiones permitidas, en
 * application.properties -> app.documentos.extensiones-permitidas
 * (solo se aceptan extensiones que esten en el mapa TIPOS de abajo).
 * NO MODIFICAR: la revision de la firma.
 */
@Component
public class ArchivoDocumentoValidador {

    /** Tamano maximo de un documento: 20 MB (igual que spring.servlet.multipart). */
    public static final long TAMANO_MAXIMO = 20L * 1024 * 1024;

    private enum Firma { PDF, ZIP, OLE }

    private record Tipo(String mime, Firma firma) {
    }

    /** Extensiones conocidas, su tipo MIME para la descarga y la firma esperada. */
    private static final Map<String, Tipo> TIPOS = Map.of(
            "pdf", new Tipo("application/pdf", Firma.PDF),
            "docx", new Tipo("application/vnd.openxmlformats-officedocument.wordprocessingml.document", Firma.ZIP),
            "xlsx", new Tipo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", Firma.ZIP),
            "xlsm", new Tipo("application/vnd.ms-excel.sheet.macroEnabled.12", Firma.ZIP),
            "doc", new Tipo("application/msword", Firma.OLE),
            "xls", new Tipo("application/vnd.ms-excel", Firma.OLE)
    );

    private final Set<String> permitidas;

    public ArchivoDocumentoValidador(
            @Value("${app.documentos.extensiones-permitidas:pdf,docx,doc,xlsx,xlsm,xls}") String configuracion) {
        this.permitidas = Arrays.stream(configuracion.split(","))
                .map(e -> e.trim().toLowerCase(Locale.ROOT))
                .filter(TIPOS::containsKey)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** Extensiones permitidas, para mostrarlas en el panel. */
    public Set<String> extensionesPermitidas() {
        return permitidas;
    }

    /**
     * Valida nombre, tamano, extension y firma del contenido.
     * Lanza IllegalArgumentException con un mensaje claro si algo falla.
     */
    public void validar(String nombreArchivo, byte[] contenido) {
        if (contenido == null || contenido.length == 0) {
            throw new IllegalArgumentException("El archivo está vacío. Vuelve a seleccionarlo e inténtalo de nuevo.");
        }
        if (contenido.length > TAMANO_MAXIMO) {
            throw new IllegalArgumentException("El archivo no puede superar los 20 MB");
        }

        String extension = extension(nombreArchivo);
        if (!permitidas.contains(extension)) {
            throw new IllegalArgumentException(
                    "Tipo de archivo no permitido. Se aceptan: " + String.join(", ", permitidas));
        }

        if (!firmaCorrecta(TIPOS.get(extension).firma(), contenido)) {
            throw new IllegalArgumentException(
                    "El contenido del archivo no corresponde a un ." + extension
                            + " válido (puede estar dañado o tener la extensión cambiada)");
        }
    }

    /** Tipo MIME con el que se entrega el archivo al descargarlo. */
    public String tipoMime(String nombreArchivo) {
        Tipo tipo = TIPOS.get(extension(nombreArchivo));
        return tipo == null ? "application/octet-stream" : tipo.mime();
    }

    /** Solo los PDF se muestran dentro del navegador; el resto se descarga. */
    public boolean esPdf(String nombreArchivo) {
        return "pdf".equals(extension(nombreArchivo));
    }

    public static String extension(String nombreArchivo) {
        if (nombreArchivo == null) {
            return "";
        }
        int punto = nombreArchivo.lastIndexOf('.');
        return punto < 0 ? "" : nombreArchivo.substring(punto + 1).toLowerCase(Locale.ROOT);
    }

    private static boolean firmaCorrecta(Firma firma, byte[] c) {
        if (c.length < 4) {
            return false;
        }
        return switch (firma) {
            case PDF -> c[0] == '%' && c[1] == 'P' && c[2] == 'D' && c[3] == 'F';
            case ZIP -> c[0] == 'P' && c[1] == 'K' && c[2] == 3 && c[3] == 4;
            case OLE -> (c[0] & 0xFF) == 0xD0 && (c[1] & 0xFF) == 0xCF
                    && (c[2] & 0xFF) == 0x11 && (c[3] & 0xFF) == 0xE0;
        };
    }
}
