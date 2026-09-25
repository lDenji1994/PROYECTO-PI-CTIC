package com.certificados.app.service;

import com.certificados.app.dto.FormatoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * CATALOGO DE FORMATOS Y REGLA DE VIGENCIA ("Vigente" / "Desactualizado").
 *
 * Los formatos NO estan quemados en el codigo: se leen de la propiedad
 * app.formatos.catalogo (application.properties o variable de entorno
 * FORMATOS_CATALOGO). Formato de cada entrada, separadas por ';':
 *
 *     CODIGO:VERSION:Nombre visible:VIGENTE|ANTIGUO
 *
 * Regla: un documento es VIGENTE solo si su codigo de formato + version
 * coinciden con una entrada marcada VIGENTE. Cualquier otro (formato
 * antiguo, version anterior o formato desconocido) es DESACTUALIZADO.
 * Las versiones se comparan como numeros cuando se puede ("03" == "3").
 *
 * SE PUEDE MODIFICAR: el catalogo en application.properties.
 * NO MODIFICAR: el orden de los campos de cada entrada.
 */
@Service
public class FormatoService {

    private static final Logger LOG = LoggerFactory.getLogger(FormatoService.class);

    private final List<FormatoDTO> catalogo;

    public FormatoService(@Value("${app.formatos.catalogo:}") String configuracion) {
        this.catalogo = Collections.unmodifiableList(leerCatalogo(configuracion));
    }

    public List<FormatoDTO> listar() {
        return catalogo;
    }

    public boolean esVigente(String codigoFormato, String versionFormato) {
        String codigo = normalizarCodigo(codigoFormato);
        String version = normalizarVersion(versionFormato);
        return catalogo.stream().anyMatch(f -> f.vigente()
                && normalizarCodigo(f.codigo()).equals(codigo)
                && normalizarVersion(f.version()).equals(version));
    }

    /** Nombre visible del formato, o null si no esta en el catalogo. */
    public String nombreDe(String codigoFormato) {
        String codigo = normalizarCodigo(codigoFormato);
        return catalogo.stream()
                .filter(f -> normalizarCodigo(f.codigo()).equals(codigo))
                .map(FormatoDTO::nombre).findFirst().orElse(null);
    }

    public static String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    public static String normalizarVersion(String version) {
        if (version == null) {
            return "";
        }
        String v = version.trim().toUpperCase(Locale.ROOT).replaceFirst("^V(ERSION)?\\s*", "");
        try {
            return String.valueOf(Integer.parseInt(v));
        } catch (NumberFormatException e) {
            return v;
        }
    }

    private static List<FormatoDTO> leerCatalogo(String configuracion) {
        List<FormatoDTO> lista = new ArrayList<>();
        if (configuracion == null || configuracion.isBlank()) {
            LOG.warn("app.formatos.catalogo esta vacio: todos los documentos se veran como desactualizados.");
            return lista;
        }
        for (String entrada : configuracion.split(";")) {
            String[] partes = entrada.trim().split(":");
            if (partes.length != 4) {
                LOG.warn("Entrada de formato ignorada (se esperan 4 campos): '{}'", entrada);
                continue;
            }
            lista.add(new FormatoDTO(
                    partes[0].trim(), partes[1].trim(), partes[2].trim(),
                    "VIGENTE".equalsIgnoreCase(partes[3].trim())));
        }
        return lista;
    }
}
