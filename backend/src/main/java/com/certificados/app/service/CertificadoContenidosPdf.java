package com.certificados.app.service;

import com.certificados.app.dto.AsignaturaCertificadoDTO;
import com.certificados.app.dto.DetalleCompletoDTO;
import com.certificados.app.model.TipoContenidoCertificado;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

/**
 * CUERPO DEL CERTIFICADO DE CONTENIDOS PROGRAMATICOS (un solo PDF por solicitud).
 *
 * Dos tipos (lo escoge la auxiliar en la solicitud):
 *   - RESUMIDO: por asignatura, la franja, los datos (creditos, horas...) y
 *               UN parrafo breve (el "resumen" de los datos del curso o, si
 *               no se escribio, la descripcion del curso).
 *   - COMPLETO: por asignatura, todo lo registrado: descripcion, proposito,
 *               justificacion, requisitos, objetivos, competencias, contenido
 *               tema por tema, metodologia, evaluacion y modalidades. Puede
 *               ocupar varias paginas por asignatura.
 *
 * Si la solicitud es por semestres o por programa completo, las asignaturas
 * salen agrupadas bajo un titulo "Semestre N".
 *
 * El encabezado del certificado (titulo, estudiante, numero de solicitud)
 * lo dibuja CertificadoGeneradorService con los elementos de la plantilla.
 * Esta clase agrega DEBAJO, y continuando en las paginas que hagan falta,
 * un bloque por cada asignatura:
 *
 *     FION 0001 - Ondas                                   (franja gris)
 *     Creditos: 3 | Horas: teoricas 3, practicas 0... | Periodo 2026-2
 *     Descripcion del curso
 *       texto...
 *     Contenido
 *       - tema 1
 *       - tema 2
 *
 * y al final la fecha de expedicion y la linea de firma.
 *
 * SE PUEDE MODIFICAR: tamanos de letra, colores y textos; la ciudad y el
 * cargo de la firma se cambian en application.properties
 * (app.certificados.ciudad / app.certificados.cargo-firma).
 * NO MODIFICAR: el uso de PdfPTable para la franja y el espacio inicial
 * (es lo que evita que el cuerpo se monte sobre el encabezado).
 */
@Component
public class CertificadoContenidosPdf {

    private static final Color GRIS_FRANJA = new Color(236, 239, 243);
    private static final Color GRIS_TEXTO = new Color(90, 98, 112);
    /** Magenta de la paleta UPB (mismo --color-primario del panel). SE PUEDE MODIFICAR. */
    private static final Color COLOR_UPB = new Color(194, 25, 142);

    private static final Font TITULO_SEMESTRE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12.5f, Font.NORMAL, COLOR_UPB);
    private static final Font TITULO_ASIGNATURA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11f);
    private static final Font ETIQUETA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, Font.NORMAL, COLOR_UPB);
    private static final Font TEXTO = FontFactory.getFont(FontFactory.HELVETICA, 9.5f);
    private static final Font TEXTO_GRIS = FontFactory.getFont(FontFactory.HELVETICA, 9f, Font.NORMAL, GRIS_TEXTO);

    private final String ciudad;
    private final String cargoFirma;

    public CertificadoContenidosPdf(
            @Value("${app.certificados.ciudad:Bucaramanga}") String ciudad,
            @Value("${app.certificados.cargo-firma:Responsable académico}") String cargoFirma) {
        this.ciudad = ciudad;
        this.cargoFirma = cargoFirma;
    }

    /**
     * @param document   documento PDF ya abierto
     * @param inicioY    distancia desde el borde superior de la primera pagina
     *                   donde termina el encabezado de la plantilla (en puntos)
     * @param asignaturas contenido ya validado (ContenidoCertificadoService),
     *                    ordenado por semestre (si aplica) y codigo
     * @param tipo        RESUMIDO o COMPLETO
     * @param alcance     texto para la introduccion, ej. "del programa Ingeniería
     *                    de Sistemas (semestres 1, 2)"; vacio si es por asignaturas
     * @param detalle     da el detalle COMPLETO de una version (solo se usa en COMPLETO)
     */
    public void agregar(Document document, float inicioY,
                        List<AsignaturaCertificadoDTO> asignaturas,
                        TipoContenidoCertificado tipo,
                        String alcance,
                        Function<Integer, DetalleCompletoDTO> detalle) throws DocumentException {

        boolean completo = tipo == TipoContenidoCertificado.COMPLETO;

        // Espacio en blanco para empezar debajo del encabezado de la plantilla
        float alto = inicioY - document.topMargin();
        if (alto > 0) {
            PdfPTable espacio = new PdfPTable(1);
            espacio.setWidthPercentage(100f);
            PdfPCell celda = new PdfPCell(new Phrase(" "));
            celda.setBorder(Rectangle.NO_BORDER);
            celda.setFixedHeight(alto);
            espacio.addCell(celda);
            document.add(espacio);
        }

        Paragraph intro = new Paragraph(limpiar(
                "A continuación se relacionan los contenidos programáticos "
                        + (completo ? "completos" : "resumidos") + " de "
                        + (asignaturas.size() == 1 ? "la asignatura registrada" : "las "
                        + asignaturas.size() + " asignaturas registradas") + " en la solicitud"
                        + (alcance == null || alcance.isBlank() ? "" : " " + alcance) + ":"), TEXTO);
        intro.setSpacingAfter(10f);
        document.add(intro);

        Integer semestreActual = null;
        for (AsignaturaCertificadoDTO asignatura : asignaturas) {
            // Titulo "Semestre N" cada vez que cambia el semestre (solicitud por semestres/programa)
            if (asignatura.semestre() != null && !Objects.equals(asignatura.semestre(), semestreActual)) {
                semestreActual = asignatura.semestre();
                agregarTituloGrupo(document, "Semestre " + semestreActual);
            } else if (asignatura.semestre() == null && semestreActual != null) {
                // Asignaturas agregadas a mano que no estan en el plan del programa
                semestreActual = null;
                agregarTituloGrupo(document, "Otras asignaturas");
            }
            if (completo) {
                agregarAsignaturaCompleta(document, asignatura, detalle.apply(asignatura.idVersionDocumento()));
            } else {
                agregarAsignaturaResumida(document, asignatura);
            }
        }

        agregarCierre(document);
    }

    private static void agregarTituloGrupo(Document document, String texto) throws DocumentException {
        Paragraph titulo = new Paragraph(texto, TITULO_SEMESTRE);
        titulo.setSpacingBefore(14f);
        titulo.setSpacingAfter(2f);
        document.add(titulo);
    }

    /** RESUMIDO: franja + datos + un parrafo breve. */
    private void agregarAsignaturaResumida(Document document, AsignaturaCertificadoDTO a) throws DocumentException {
        agregarEncabezadoAsignatura(document, a);
        String parrafo = a.resumen() != null && !a.resumen().isBlank() ? a.resumen() : a.descripcion();
        Paragraph texto = new Paragraph(limpiar(parrafo), TEXTO);
        texto.setAlignment(Element.ALIGN_JUSTIFIED);
        texto.setSpacingAfter(4f);
        document.add(texto);
    }

    /** COMPLETO: franja + datos + todas las secciones que tengan informacion. */
    private void agregarAsignaturaCompleta(Document document, AsignaturaCertificadoDTO a,
                                           DetalleCompletoDTO d) throws DocumentException {
        agregarEncabezadoAsignatura(document, a);

        seccionTexto(document, "Descripción del curso", a.descripcion());
        seccionTexto(document, "Propósito de formación", d.proposito());
        seccionTexto(document, "Justificación", d.justificacion());
        seccionTexto(document, "Requisitos / saberes previos", d.requisitos());
        seccionLista(document, "Objetivos", d.objetivos());

        if (!d.competencias().isEmpty()) {
            document.add(etiqueta("Competencias"));
            for (DetalleCompletoDTO.CompetenciaDetalle c : d.competencias()) {
                document.add(item(c.descripcion(), 12f));
                for (String criterio : c.criterios()) {
                    document.add(item(criterio, 26f));
                }
            }
        }

        // Contenido tema por tema (con semana / unidad / tema si se registraron)
        document.add(etiqueta("Contenido"));
        if (d.contenidos().isEmpty()) {
            for (String tema : a.contenidos()) {
                document.add(item(tema, 12f));
            }
        } else {
            for (DetalleCompletoDTO.TemaDetalle t : d.contenidos()) {
                StringBuilder linea = new StringBuilder();
                if (t.semana() != null) {
                    linea.append("Semana ").append(t.semana()).append(": ");
                }
                if (t.unidad() != null && !t.unidad().isBlank()) {
                    linea.append(t.unidad().trim()).append(" - ");
                }
                if (t.tema() != null && !t.tema().isBlank()) {
                    linea.append(t.tema().trim()).append(". ");
                }
                linea.append(t.descripcion().trim());
                document.add(item(linea.toString(), 12f));
            }
        }

        seccionLista(document, "Metodología", d.metodologias());

        if (!d.evaluaciones().isEmpty()) {
            document.add(etiqueta("Evaluación"));
            for (DetalleCompletoDTO.EvaluacionDetalle e : d.evaluaciones()) {
                String linea = (e.tipo() == null || e.tipo().isBlank() ? "" : e.tipo().trim() + ": ")
                        + e.descripcion().trim()
                        + (e.porcentaje() == null ? "" : " (" + numero(e.porcentaje()) + " %)");
                document.add(item(linea, 12f));
            }
        }

        seccionTexto(document, "Modalidades", d.modalidades());
    }

    private void seccionTexto(Document document, String titulo, String texto) throws DocumentException {
        if (texto == null || texto.isBlank()) {
            return;
        }
        document.add(etiqueta(titulo));
        Paragraph p = new Paragraph(limpiar(texto), TEXTO);
        p.setAlignment(Element.ALIGN_JUSTIFIED);
        p.setSpacingAfter(4f);
        document.add(p);
    }

    private void seccionLista(Document document, String titulo, List<String> lineas) throws DocumentException {
        if (lineas == null || lineas.isEmpty()) {
            return;
        }
        document.add(etiqueta(titulo));
        for (String linea : lineas) {
            document.add(item(linea, 12f));
        }
    }

    private static Paragraph etiqueta(String titulo) {
        Paragraph p = new Paragraph(titulo, ETIQUETA);
        p.setSpacingBefore(4f);
        return p;
    }

    private static Paragraph item(String texto, float sangria) {
        Paragraph linea = new Paragraph(vineta(limpiar(texto)), TEXTO);
        linea.setIndentationLeft(sangria);
        linea.setAlignment(Element.ALIGN_JUSTIFIED);
        return linea;
    }

    /** Franja con el codigo (materia + curso) y el nombre, y la linea de datos. */
    private void agregarEncabezadoAsignatura(Document document, AsignaturaCertificadoDTO a) throws DocumentException {
        PdfPTable franja = new PdfPTable(1);
        franja.setWidthPercentage(100f);
        franja.setSpacingBefore(8f);
        PdfPCell celda = new PdfPCell(new Phrase(limpiar(a.codigo() + "  -  " + a.nombre()), TITULO_ASIGNATURA));
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setBackgroundColor(GRIS_FRANJA);
        celda.setPadding(6f);
        franja.addCell(celda);
        document.add(franja);

        Paragraph datos = new Paragraph(limpiar(resumenDatos(a)), TEXTO_GRIS);
        datos.setSpacingBefore(4f);
        datos.setSpacingAfter(4f);
        document.add(datos);
    }

    private void agregarCierre(Document document) throws DocumentException {
        LocalDate hoy = LocalDate.now();
        String mes = hoy.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es"));

        Paragraph expedicion = new Paragraph(limpiar("Se expide en " + ciudad + ", a los "
                + hoy.getDayOfMonth() + " días del mes de " + mes + " de " + hoy.getYear() + "."), TEXTO);
        expedicion.setSpacingBefore(22f);
        expedicion.setSpacingAfter(46f);
        document.add(expedicion);

        Paragraph linea = new Paragraph("______________________________", TEXTO);
        linea.setAlignment(Element.ALIGN_CENTER);
        document.add(linea);

        Paragraph cargo = new Paragraph(limpiar(cargoFirma), TEXTO_GRIS);
        cargo.setAlignment(Element.ALIGN_CENTER);
        document.add(cargo);
    }

    /** "Créditos: 3  |  Horas: teóricas 3, independientes 6  |  Periodo: 2026-2  |  Fuente: ..." */
    private static String resumenDatos(AsignaturaCertificadoDTO a) {
        StringBuilder texto = new StringBuilder("Créditos: ").append(numero(a.creditos()));

        StringBuilder horas = new StringBuilder();
        agregarHora(horas, "teóricas", a.horasTeoricas());
        agregarHora(horas, "prácticas", a.horasPracticas());
        agregarHora(horas, "laboratorio", a.horasLaboratorio());
        agregarHora(horas, "trabajo independiente", a.horasIndependientes());
        if (horas.length() > 0) {
            texto.append("   |   Horas: ").append(horas);
        }
        if (a.periodo() != null && !a.periodo().isBlank()) {
            texto.append("   |   Periodo: ").append(a.periodo());
        }
        if (a.tipoDocumento() != null) {
            texto.append("   |   Fuente: ").append(a.tipoDocumento()).append(" ").append(a.formato());
        }
        return texto.toString();
    }

    private static void agregarHora(StringBuilder horas, String nombre, BigDecimal valor) {
        if (valor != null && valor.signum() > 0) {
            if (horas.length() > 0) {
                horas.append(", ");
            }
            horas.append(nombre).append(' ').append(numero(valor));
        }
    }

    /** 3.00 -> "3",  2.50 -> "2.5" */
    private static String numero(BigDecimal valor) {
        return valor == null ? "-" : valor.stripTrailingZeros().toPlainString();
    }

    /** Si el tema ya trae numero o vineta ("1.", "a)", "-", "•") se deja igual. */
    private static String vineta(String tema) {
        return tema.matches("^(\\d+[.)\\-]|[a-zA-Z][.)]|[-•*]).*") ? tema : "•  " + tema;
    }

    /**
     * La fuente del PDF solo conoce los caracteres de Europa occidental.
     * Se cambian los simbolos mas comunes que quedarian en blanco y se
     * descarta cualquier otro caracter que la fuente no pueda dibujar.
     */
    static String limpiar(String texto) {
        if (texto == null) {
            return "";
        }
        String t = texto
                .replace('\t', ' ')
                .replace("→", "->").replace("←", "<-")
                .replace("≥", ">=").replace("≤", "<=")
                .replace("•", "•")
                .replace(' ', ' ').replace(' ', ' ').replace(' ', ' ');
        StringBuilder limpio = new StringBuilder(t.length());
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            boolean dibujable = c == '\n' || (c >= 32 && c < 127) || (c >= 160 && c <= 255)
                    || "€‚ƒ„…†‡ˆ‰Š‹ŒŽ‘’“”•–—˜™š›œžŸ".indexOf(c) >= 0;
            limpio.append(dibujable ? c : ' ');
        }
        return limpio.toString();
    }
}
