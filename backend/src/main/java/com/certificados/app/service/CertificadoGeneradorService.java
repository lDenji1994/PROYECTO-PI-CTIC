package com.certificados.app.service;

import com.certificados.app.exception.BusinessException;
import com.certificados.app.exception.ResourceNotFoundException;
import com.certificados.app.model.CampoPlantilla;
import com.certificados.app.model.CertificadoGenerado;
import com.certificados.app.model.ElementoPlantilla;
import com.certificados.app.model.SolicitudCertificado;
import com.certificados.app.model.VersionPlantilla;
import com.certificados.app.repository.CampoPlantillaRepository;
import com.certificados.app.repository.CertificadoGeneradoRepository;
import com.certificados.app.repository.ElementoPlantillaRepository;
import com.certificados.app.repository.SolicitudCertificadoRepository;
import com.certificados.app.repository.VersionPlantillaRepository;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class CertificadoGeneradorService {

    private final CertificadoGeneradoRepository certificadoRepository;
    private final SolicitudCertificadoRepository solicitudRepository;
    private final VersionPlantillaRepository versionPlantillaRepository;
    private final ElementoPlantillaRepository elementoRepository;
    private final CampoPlantillaRepository campoRepository;

    public CertificadoGeneradorService(
            CertificadoGeneradoRepository certificadoRepository,
            SolicitudCertificadoRepository solicitudRepository,
            VersionPlantillaRepository versionPlantillaRepository,
            ElementoPlantillaRepository elementoRepository,
            CampoPlantillaRepository campoRepository) {

        this.certificadoRepository = certificadoRepository;
        this.solicitudRepository = solicitudRepository;
        this.versionPlantillaRepository =
                versionPlantillaRepository;
        this.elementoRepository = elementoRepository;
        this.campoRepository = campoRepository;
    }

    public CertificadoGenerado generar(
            Integer idSolicitud,
            Integer idVersionPlantilla) {

        SolicitudCertificado solicitud =
                solicitudRepository.findById(idSolicitud)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Solicitud de certificado no encontrada con id "
                                                + idSolicitud
                                ));

        VersionPlantilla version =
                versionPlantillaRepository.findById(
                                idVersionPlantilla
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Versión de plantilla no encontrada con id "
                                                + idVersionPlantilla
                                ));

        if (certificadoRepository
                .findByIdSolicitudCertificado(idSolicitud)
                .isPresent()) {

            throw new BusinessException(
                    "La solicitud ya tiene un certificado generado"
            );
        }

        List<ElementoPlantilla> elementos =
                elementoRepository.findByIdVersionPlantilla(
                        idVersionPlantilla
                );

        if (elementos.isEmpty()) {
            throw new BusinessException(
                    "La versión de plantilla no tiene elementos configurados"
            );
        }

        elementos.sort(
                Comparator.comparing(
                        ElementoPlantilla::getOrden,
                        Comparator.nullsLast(Integer::compareTo)
                )
        );

        try {

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            Document document =
                    new Document(PageSize.A4);

            PdfWriter writer =
                    PdfWriter.getInstance(
                            document,
                            outputStream
                    );

            document.open();

            PdfContentByte canvas =
                    writer.getDirectContent();

            for (ElementoPlantilla elemento : elementos) {

                renderizarElemento(
                        canvas,
                        elemento,
                        solicitud
                );
            }

            document.close();

            byte[] archivo =
                    outputStream.toByteArray();

            CertificadoGenerado certificado =
                    new CertificadoGenerado();

            certificado.setNombreArchivo(
                    "certificado-solicitud-"
                            + idSolicitud
                            + ".pdf"
            );

            certificado.setArchivo(archivo);

            certificado.setIdSolicitudCertificado(
                    idSolicitud
            );

            certificado.setIdVersionPlantilla(
                    idVersionPlantilla
            );

            return certificadoRepository.save(
                    certificado
            );

        } catch (Exception e) {

            throw new BusinessException(
                    "No fue posible generar el certificado: "
                            + e.getMessage()
            );
        }
    }

    private void renderizarElemento(
            PdfContentByte canvas,
            ElementoPlantilla elemento,
            SolicitudCertificado solicitud) {

        if (elemento.getTipoElemento() == null) {
            return;
        }

        switch (elemento.getTipoElemento()) {

            case TEXTO:
            case CAMPO:
                renderizarTexto(
                        canvas,
                        elemento,
                        solicitud
                );
                break;

            case LINEA:
                renderizarLinea(
                        canvas,
                        elemento
                );
                break;

            case IMAGEN:
            case FIRMA:
            case TABLA:
                throw new BusinessException(
                        "El tipo de elemento "
                                + elemento.getTipoElemento()
                                + " todavía no está soportado por el motor PDF"
                );
        }
    }

    private void renderizarTexto(
            PdfContentByte canvas,
            ElementoPlantilla elemento,
            SolicitudCertificado solicitud) {

        String contenido =
                resolverContenido(
                        elemento,
                        solicitud
                );

        if (contenido == null
                || contenido.isBlank()) {
            return;
        }

        /*
         * X representa la posición horizontal
         * desde el borde izquierdo.
         */
        float x =
                decimalToFloat(
                        elemento.getPosicionX(),
                        50
                );

        /*
         * En la plantilla Y se interpreta
         * desde la parte superior.
         *
         * PDF trabaja desde la parte inferior,
         * por eso invertimos el eje Y.
         */
        float y =
                PageSize.A4.getHeight()
                        - decimalToFloat(
                                elemento.getPosicionY(),
                                50
                        );

        float tamanoFuente =
                decimalToFloat(
                        elemento.getTamanoFuente(),
                        12
                );

        BaseFont fuente =
                obtenerFuente(
                        elemento.getTipoFuente()
                );

        canvas.beginText();

        canvas.setFontAndSize(
                fuente,
                tamanoFuente
        );

        /*
         * Ancho disponible para el elemento.
         */
        float ancho =
                decimalToFloat(
                        elemento.getAncho(),
                        0
                );

        int alineacion =
                obtenerAlineacion(
                        elemento.getAlineacion()
                );

        /*
         * Calculamos el ancho real del texto
         * para poder centrarlo o alinearlo
         * a la derecha dentro del elemento.
         */
        float anchoTexto =
                fuente.getWidthPoint(
                        contenido,
                        tamanoFuente
                );

        if (ancho > 0) {

            if (alineacion
                    == PdfContentByte.ALIGN_CENTER) {

                x = x + ((ancho - anchoTexto) / 2);

            } else if (alineacion
                    == PdfContentByte.ALIGN_RIGHT) {

                x = x + ancho - anchoTexto;
            }
        }

        canvas.setTextMatrix(
                x,
                y
        );

        canvas.showText(
                contenido
        );

        canvas.endText();
    }

    private void renderizarLinea(
            PdfContentByte canvas,
            ElementoPlantilla elemento) {

        float x =
                decimalToFloat(
                        elemento.getPosicionX(),
                        50
                );

        /*
         * Conversión del eje Y
         * de coordenadas de plantilla
         * a coordenadas PDF.
         */
        float y =
                PageSize.A4.getHeight()
                        - decimalToFloat(
                                elemento.getPosicionY(),
                                50
                        );

        float ancho =
                decimalToFloat(
                        elemento.getAncho(),
                        100
                );

        float alto =
                decimalToFloat(
                        elemento.getAlto(),
                        0
                );

        canvas.moveTo(
                x,
                y
        );

        canvas.lineTo(
                x + ancho,
                y - alto
        );

        canvas.stroke();
    }

    private String resolverContenido(
            ElementoPlantilla elemento,
            SolicitudCertificado solicitud) {

        if (elemento.getTipoElemento()
                == ElementoPlantilla.TipoElemento.TEXTO) {

            return elemento.getContenido();
        }

        if (elemento.getTipoElemento()
                == ElementoPlantilla.TipoElemento.CAMPO) {

            if (elemento.getIdCampoPlantilla() == null) {
                throw new BusinessException(
                        "El elemento CAMPO no tiene un campo asociado"
                );
            }

            CampoPlantilla campo =
                    campoRepository.findById(
                                    elemento.getIdCampoPlantilla()
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Campo de plantilla no encontrado con id "
                                                    + elemento.getIdCampoPlantilla()
                                    ));

            return resolverCampo(
                    campo,
                    solicitud
            );
        }

        return elemento.getContenido();
    }

    private String resolverCampo(
            CampoPlantilla campo,
            SolicitudCertificado solicitud) {

        /*
         * El ID del estudiante será posteriormente
         * utilizado para consultar Kárdex.
         *
         * No se crea una tabla Estudiantes en esta BD.
         */
        if ("ESTUDIANTE".equalsIgnoreCase(
                campo.getFuente())) {

            return "Estudiante ID: "
                    + solicitud.getIdEstudiante();
        }

        if ("SOLICITUD".equalsIgnoreCase(
                campo.getFuente())) {

            return String.valueOf(
                    solicitud.getId()
            );
        }

        if ("TIPO_CERTIFICADO".equalsIgnoreCase(
                campo.getFuente())) {

            return String.valueOf(
                    solicitud.getIdTipoCertificado()
            );
        }

        throw new BusinessException(
                "No existe un resolver para la fuente "
                        + campo.getFuente()
                        + " del campo "
                        + campo.getCodigoCampo()
        );
    }

    private BaseFont obtenerFuente(
            String tipoFuente) {

        try {

            if (tipoFuente == null
                    || tipoFuente.isBlank()) {

                return BaseFont.createFont(
                        BaseFont.HELVETICA,
                        BaseFont.WINANSI,
                        BaseFont.NOT_EMBEDDED
                );
            }

            String fuente =
                    tipoFuente.trim();

            if (fuente.equalsIgnoreCase("Arial")) {

                return BaseFont.createFont(
                        BaseFont.HELVETICA,
                        BaseFont.WINANSI,
                        BaseFont.NOT_EMBEDDED
                );
            }

            if (fuente.equalsIgnoreCase("Helvetica")) {

                return BaseFont.createFont(
                        BaseFont.HELVETICA,
                        BaseFont.WINANSI,
                        BaseFont.NOT_EMBEDDED
                );
            }

            if (fuente.equalsIgnoreCase("Times")) {

                return BaseFont.createFont(
                        BaseFont.TIMES_ROMAN,
                        BaseFont.WINANSI,
                        BaseFont.NOT_EMBEDDED
                );
            }

            if (fuente.equalsIgnoreCase("Courier")) {

                return BaseFont.createFont(
                        BaseFont.COURIER,
                        BaseFont.WINANSI,
                        BaseFont.NOT_EMBEDDED
                );
            }

            return BaseFont.createFont(
                    BaseFont.HELVETICA,
                    BaseFont.WINANSI,
                    BaseFont.NOT_EMBEDDED
            );

        } catch (Exception e) {

            throw new BusinessException(
                    "No fue posible cargar la fuente "
                            + tipoFuente
                            + ": "
                            + e.getMessage()
            );
        }
    }

    private int obtenerAlineacion(
            String alineacion) {

        if (alineacion == null) {
            return PdfContentByte.ALIGN_LEFT;
        }

        if (alineacion.equalsIgnoreCase("CENTER")
                || alineacion.equalsIgnoreCase("CENTRO")) {

            return PdfContentByte.ALIGN_CENTER;
        }

        if (alineacion.equalsIgnoreCase("RIGHT")
                || alineacion.equalsIgnoreCase("DERECHA")) {

            return PdfContentByte.ALIGN_RIGHT;
        }

        return PdfContentByte.ALIGN_LEFT;
    }

    private float decimalToFloat(
            BigDecimal valor,
            float valorPorDefecto) {

        if (valor == null) {
            return valorPorDefecto;
        }

        return valor.floatValue();
    }
}