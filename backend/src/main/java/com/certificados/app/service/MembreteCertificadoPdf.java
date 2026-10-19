package com.certificados.app.service;

import com.certificados.app.exception.BusinessException;
import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;

/**
 * MEMBRETE, PIE DE PAGINA Y PAGINACION DEL CERTIFICADO (todas las paginas).
 *
 * Igual al formato institucional de "Resumen contenidos programaticos":
 *
 *   [logo UPB]                        UNIVERSIDAD PONTIFICIA BOLIVARIANA
 *                                     ESCUELA DE ...
 *                                     RESUMEN CONTENIDOS PROGRAMATICOS
 *                                     FACULTAD DE ...
 *                                      Nombre del estudiante, ID. 000123456
 *   ...cuerpo...
 *   ----------------------------------------------------------------------
 *   Universidad Pontificia Bolivariana · Seccional ...      Pagina 1 de 3
 *
 * Se dibuja con un evento de pagina (onEndPage), asi sale en cada pagina sin
 * importar cuantas ocupe el contenido. El total de paginas ("de 3") se
 * escribe al cerrar el documento.
 *
 * SE PUEDE MODIFICAR en application.properties:
 *   app.certificados.membrete.escuela   (segunda linea del membrete)
 *   app.certificados.membrete.facultad  (cuarta linea del membrete)
 *   app.certificados.pie-pagina         (texto del pie de pagina)
 * El logo esta en src/main/resources/certificados/logo-upb.png (se puede
 * reemplazar por otro PNG con el mismo nombre).
 *
 * NO MODIFICAR: FIN_MEMBRETE e INICIO_PIE sin revisar los margenes del
 * documento en CertificadoGeneradorService (el cuerpo no debe montarse
 * sobre el membrete ni sobre el pie).
 */
@Component
public class MembreteCertificadoPdf {

    /** Distancia desde el borde superior donde termina el membrete (puntos). */
    public static final float FIN_MEMBRETE = 112f;
    /** Distancia desde el borde inferior donde empieza el pie de pagina (puntos). */
    public static final float INICIO_PIE = 50f;

    private static final String RUTA_LOGO = "certificados/logo-upb.png";
    private static final float MARGEN_LATERAL = 50f;
    private static final float MARGEN_SUPERIOR = 30f;
    private static final float ANCHO_LOGO = 125f;
    private static final float ALTO_LOGO = 62f;
    private static final float LETRA_MEMBRETE = 9.5f;
    private static final float LETRA_PIE = 7.5f;
    private static final Color GRIS = new Color(110, 116, 128);
    private static final Color GRIS_LINEA = new Color(200, 204, 210);

    private final String escuela;
    private final String facultad;
    private final String piePagina;
    private volatile byte[] logo;

    public MembreteCertificadoPdf(
            @Value("${app.certificados.membrete.escuela:ESCUELA DE INGENIERÍAS}") String escuela,
            @Value("${app.certificados.membrete.facultad:FACULTAD DE INGENIERÍA DE SISTEMAS E INFORMÁTICA}") String facultad,
            @Value("${app.certificados.pie-pagina:Universidad Pontificia Bolivariana · Seccional Bucaramanga · Km 7 Vía a Piedecuesta, Santander, Colombia}") String piePagina) {
        this.escuela = escuela;
        this.facultad = facultad;
        this.piePagina = piePagina;
    }

    /** Textos del membrete y del pie (el panel los muestra en la vista previa). */
    public String escuela() {
        return escuela;
    }

    public String facultad() {
        return facultad;
    }

    public String piePagina() {
        return piePagina;
    }

    /**
     * Evento para UN documento (guarda el contador de paginas).
     *
     * @param titulo     tercera linea, ej. "RESUMEN CONTENIDOS PROGRAMÁTICOS"
     * @param estudiante linea del estudiante, ej. "Ana Pérez, ID. 000123456"
     */
    public PdfPageEventHelper crearEvento(String titulo, String estudiante) {
        return new Evento(titulo, estudiante, logo());
    }

    private byte[] logo() {
        byte[] actual = logo;
        if (actual == null) {
            try (InputStream entrada = new ClassPathResource(RUTA_LOGO).getInputStream()) {
                actual = entrada.readAllBytes();
                logo = actual;
            } catch (IOException e) {
                throw new BusinessException("No se encontró el logo del membrete (" + RUTA_LOGO + ").");
            }
        }
        return actual;
    }

    private final class Evento extends PdfPageEventHelper {

        private final String titulo;
        private final String estudiante;
        private final byte[] bytesLogo;
        private BaseFont normal;
        private BaseFont negrita;
        private BaseFont cursiva;
        private PdfTemplate totalPaginas;

        private Evento(String titulo, String estudiante, byte[] bytesLogo) {
            this.titulo = titulo;
            this.estudiante = estudiante;
            this.bytesLogo = bytesLogo;
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            try {
                normal = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
                negrita = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
                cursiva = BaseFont.createFont(BaseFont.HELVETICA_OBLIQUE, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            } catch (Exception e) {
                throw new IllegalStateException("No fue posible cargar las fuentes del membrete", e);
            }
            // Espacio reservado para el total de paginas; se llena en onCloseDocument
            totalPaginas = writer.getDirectContent().createTemplate(30, 12);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte canvas = writer.getDirectContent();
            float anchoPagina = document.getPageSize().getWidth();
            float altoPagina = document.getPageSize().getHeight();
            dibujarMembrete(canvas, anchoPagina, altoPagina);
            dibujarPie(canvas, anchoPagina, writer.getPageNumber());
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            totalPaginas.beginText();
            totalPaginas.setFontAndSize(normal, LETRA_PIE);
            totalPaginas.setColorFill(GRIS);
            totalPaginas.setTextMatrix(0, 2);
            totalPaginas.showText(String.valueOf(writer.getPageNumber() - 1));
            totalPaginas.endText();
        }

        private void dibujarMembrete(PdfContentByte canvas, float anchoPagina, float altoPagina) {
            // Logo a la izquierda
            try {
                Image imagen = Image.getInstance(bytesLogo);
                imagen.scaleToFit(ANCHO_LOGO, ALTO_LOGO);
                imagen.setAbsolutePosition(MARGEN_LATERAL, altoPagina - MARGEN_SUPERIOR - imagen.getScaledHeight());
                canvas.addImage(imagen);
            } catch (Exception e) {
                throw new IllegalStateException("No fue posible dibujar el logo del membrete", e);
            }

            // Cuatro lineas en negrita alineadas a la derecha
            float derecha = anchoPagina - MARGEN_LATERAL;
            float anchoTexto = derecha - (MARGEN_LATERAL + ANCHO_LOGO + 15f);
            String[] lineas = {
                    "UNIVERSIDAD PONTIFICIA BOLIVARIANA",
                    escuela,
                    titulo,
                    facultad
            };
            float y = altoPagina - MARGEN_SUPERIOR - 9f;
            canvas.saveState();
            canvas.setColorFill(Color.BLACK);
            for (String linea : lineas) {
                escribirDerecha(canvas, negrita, linea, LETRA_MEMBRETE, derecha, y, anchoTexto);
                y -= 12f;
            }
            // Estudiante en cursiva, debajo del logo
            if (estudiante != null && !estudiante.isBlank()) {
                escribirDerecha(canvas, cursiva, estudiante, LETRA_MEMBRETE, derecha,
                        altoPagina - FIN_MEMBRETE + 10f, anchoPagina - 2 * MARGEN_LATERAL);
            }
            canvas.restoreState();
        }

        private void dibujarPie(PdfContentByte canvas, float anchoPagina, int pagina) {
            float izquierda = MARGEN_LATERAL;
            float derecha = anchoPagina - MARGEN_LATERAL;

            canvas.saveState();
            canvas.setColorStroke(GRIS_LINEA);
            canvas.setLineWidth(0.6f);
            canvas.moveTo(izquierda, INICIO_PIE);
            canvas.lineTo(derecha, INICIO_PIE);
            canvas.stroke();

            float y = INICIO_PIE - 13f;
            String prefijo = "Página " + pagina + " de ";
            float anchoTotal = normal.getWidthPoint("999", LETRA_PIE);
            float anchoPrefijo = normal.getWidthPoint(prefijo, LETRA_PIE);
            float inicioNumero = derecha - anchoTotal - anchoPrefijo;

            canvas.setColorFill(GRIS);
            canvas.beginText();
            // Pie a la izquierda (se reduce la letra si no cabe antes de la paginacion)
            String pie = CertificadoContenidosPdf.limpiar(piePagina);
            float maximo = inicioNumero - izquierda - 12f;
            float tamano = ajustar(normal, pie, LETRA_PIE, maximo);
            canvas.setFontAndSize(normal, tamano);
            canvas.showTextAligned(PdfContentByte.ALIGN_LEFT, pie, izquierda, y, 0);
            // "Pagina N de " + total (se completa al cerrar el documento)
            canvas.setFontAndSize(normal, LETRA_PIE);
            canvas.showTextAligned(PdfContentByte.ALIGN_LEFT, prefijo, inicioNumero, y, 0);
            canvas.endText();
            canvas.addTemplate(totalPaginas, inicioNumero + anchoPrefijo, y - 2);
            canvas.restoreState();
        }

        private void escribirDerecha(PdfContentByte canvas, BaseFont fuente, String texto, float tamano,
                                     float derecha, float y, float anchoMaximo) {
            String limpio = CertificadoContenidosPdf.limpiar(texto);
            canvas.beginText();
            canvas.setFontAndSize(fuente, ajustar(fuente, limpio, tamano, anchoMaximo));
            canvas.showTextAligned(PdfContentByte.ALIGN_RIGHT, limpio, derecha, y, 0);
            canvas.endText();
        }

        /** Reduce la letra (hasta 6 pt) si el texto no cabe en el ancho disponible. */
        private float ajustar(BaseFont fuente, String texto, float tamano, float anchoMaximo) {
            float actual = tamano;
            while (actual > 6f && fuente.getWidthPoint(texto, actual) > anchoMaximo) {
                actual -= 0.25f;
            }
            return actual;
        }
    }
}
