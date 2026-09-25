package com.certificados.app.service;

import com.certificados.app.dto.DocumentoCertificadoDTO;
import com.certificados.app.dto.InformacionDocumentoCertificadoDTO;
import com.certificados.app.model.CampoPlantilla;
import com.certificados.app.model.DetalleSolicitudCertificado;
import com.certificados.app.model.DocumentoAcademico;
import com.certificados.app.model.PlantillaCertificado;
import com.certificados.app.model.SolicitudCertificado;
import com.certificados.app.model.TipoDocumentoAcademico;
import com.certificados.app.model.VersionDocumento;
import com.certificados.app.model.VersionPlantilla;
import com.certificados.app.repository.CampoPlantillaRepository;
import com.certificados.app.repository.DetalleSolicitudCertificadoRepository;
import com.certificados.app.repository.DocumentoAcademicoRepository;
import com.certificados.app.repository.PlantillaCertificadoRepository;
import com.certificados.app.repository.SolicitudCertificadoRepository;
import com.certificados.app.repository.TipoDocumentoAcademicoRepository;
import com.certificados.app.repository.VersionDocumentoRepository;
import com.certificados.app.repository.VersionPlantillaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class DocumentoCertificadoService {

    private final DetalleSolicitudCertificadoRepository detalleRepository;
    private final DocumentoAcademicoRepository documentoRepository;
    private final TipoDocumentoAcademicoRepository tipoDocumentoRepository;
    private final VersionDocumentoRepository versionRepository;

    private final SolicitudCertificadoRepository solicitudRepository;
    private final PlantillaCertificadoRepository plantillaRepository;
    private final VersionPlantillaRepository versionPlantillaRepository;
    private final CampoPlantillaRepository campoPlantillaRepository;

    public DocumentoCertificadoService(
            DetalleSolicitudCertificadoRepository detalleRepository,
            DocumentoAcademicoRepository documentoRepository,
            TipoDocumentoAcademicoRepository tipoDocumentoRepository,
            VersionDocumentoRepository versionRepository,
            SolicitudCertificadoRepository solicitudRepository,
            PlantillaCertificadoRepository plantillaRepository,
            VersionPlantillaRepository versionPlantillaRepository,
            CampoPlantillaRepository campoPlantillaRepository) {

        this.detalleRepository = detalleRepository;
        this.documentoRepository = documentoRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.versionRepository = versionRepository;

        this.solicitudRepository = solicitudRepository;
        this.plantillaRepository = plantillaRepository;
        this.versionPlantillaRepository = versionPlantillaRepository;
        this.campoPlantillaRepository = campoPlantillaRepository;
    }

    /**
     * Identifica los documentos académicos asociados a las asignaturas
     * incluidas en una solicitud.
     *
     * También informa si existe una versión cargada del documento.
     */
    public List<DocumentoCertificadoDTO> obtenerDocumentosPorSolicitud(
            Integer idSolicitud) {

        List<DetalleSolicitudCertificado> detalles =
                detalleRepository.findByIdSolicitudCertificado(idSolicitud);

        List<DocumentoCertificadoDTO> resultado =
                new ArrayList<>();

        List<TipoDocumentoAcademico> tipos =
                tipoDocumentoRepository.findAll();

        for (DetalleSolicitudCertificado detalle : detalles) {

            Integer idAsignatura =
                    detalle.getIdAsignatura();

            List<DocumentoAcademico> documentos =
                    documentoRepository.findByIdAsignatura(idAsignatura);

            for (TipoDocumentoAcademico tipo : tipos) {

                DocumentoAcademico documento =
                        documentos.stream()
                                .filter(d ->
                                        d.getIdTipoDocumentoAcademico()
                                                .equals(tipo.getId()))
                                .findFirst()
                                .orElse(null);

                DocumentoCertificadoDTO dto =
                        construirDTO(
                                idAsignatura,
                                tipo,
                                documento
                        );

                resultado.add(dto);
            }
        }

        return resultado;
    }

    /**
     * Obtiene la información documental requerida por el tipo de
     * certificado de una solicitud.
     *
     * El flujo es:
     *
     * Solicitud
     *     -> Tipo de certificado
     *     -> Plantilla
     *     -> Última versión de plantilla
     *     -> Campos DOCUMENTO_ACADEMICO
     *     -> Asignaturas de la solicitud
     *     -> Documentos académicos
     *     -> Última versión disponible
     *     -> reglaObtencion
     *     -> valor
     */
    public List<InformacionDocumentoCertificadoDTO>
    obtenerInformacionRequerida(Integer idSolicitud) {

        SolicitudCertificado solicitud =
                solicitudRepository.findById(idSolicitud)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Solicitud de certificado no encontrada con id "
                                                + idSolicitud));

        List<PlantillaCertificado> plantillas =
                plantillaRepository.findByIdTipoCertificado(
                        solicitud.getIdTipoCertificado()
                );

        if (plantillas.isEmpty()) {
            throw new IllegalArgumentException(
                    "No existe una plantilla asociada al tipo de certificado "
                            + solicitud.getIdTipoCertificado()
            );
        }

        PlantillaCertificado plantilla = plantillas.get(0);

        List<VersionPlantilla> versiones =
                versionPlantillaRepository
                        .findByIdPlantillaCertificadoOrderByFechaCargaDesc(
                                plantilla.getId()
                        );

        if (versiones.isEmpty()) {
            throw new IllegalArgumentException(
                    "La plantilla no tiene versiones disponibles"
            );
        }

        VersionPlantilla versionPlantilla =
                versiones.get(0);

        List<CampoPlantilla> campos =
                campoPlantillaRepository
                        .findByIdVersionPlantilla(
                                versionPlantilla.getId()
                        );

        List<InformacionDocumentoCertificadoDTO> resultado =
                new ArrayList<>();

        List<DetalleSolicitudCertificado> detalles =
                detalleRepository.findByIdSolicitudCertificado(
                        idSolicitud
                );

        for (CampoPlantilla campo : campos) {

            if (campo.getFuente() == null
                    || !"DOCUMENTO_ACADEMICO".equalsIgnoreCase(
                            campo.getFuente())) {
                continue;
            }

            for (DetalleSolicitudCertificado detalle : detalles) {

                Integer idAsignatura =
                        detalle.getIdAsignatura();

                List<DocumentoAcademico> documentos =
                        documentoRepository.findByIdAsignatura(
                                idAsignatura
                        );

                if (documentos.isEmpty()) {
                    InformacionDocumentoCertificadoDTO dto =
                            construirInformacionSinDocumento(
                                    idAsignatura,
                                    campo
                            );

                    resultado.add(dto);
                    continue;
                }

                for (DocumentoAcademico documento : documentos) {

                    VersionDocumento version =
                            obtenerUltimaVersion(
                                    documento.getId()
                            );

                    InformacionDocumentoCertificadoDTO dto =
                            construirInformacion(
                                    idAsignatura,
                                    documento,
                                    version,
                                    campo
                            );

                    resultado.add(dto);
                }
            }
        }

        return resultado;
    }

    /**
     * Obtiene la versión más reciente del documento académico.
     */
    private VersionDocumento obtenerUltimaVersion(
            Integer idDocumentoAcademico) {

        List<VersionDocumento> versiones =
                versionRepository.findByIdDocumentoAcademico(
                        idDocumentoAcademico
                );

        return versiones.stream()
                .filter(v -> v.getFechaCarga() != null)
                .max(
                        Comparator
                                .comparing(
                                        VersionDocumento::getFechaCarga
                                )
                                .thenComparing(
                                        VersionDocumento::getId
                                )
                )
                .orElse(null);
    }

    /**
     * Construye la respuesta cuando el documento existe.
     */
    private InformacionDocumentoCertificadoDTO construirInformacion(
            Integer idAsignatura,
            DocumentoAcademico documento,
            VersionDocumento version,
            CampoPlantilla campo) {

        InformacionDocumentoCertificadoDTO dto =
                new InformacionDocumentoCertificadoDTO();

        dto.setIdAsignatura(idAsignatura);

        dto.setIdDocumentoAcademico(
                documento.getId()
        );

        dto.setCodigoCampo(
                campo.getCodigoCampo()
        );

        dto.setNombreCampo(
                campo.getNombreCampo()
        );

        dto.setFuente(
                campo.getFuente()
        );

        dto.setMetodoObtencion(
                campo.getMetodoObtencion() != null
                        ? campo.getMetodoObtencion().name()
                        : null
        );

        dto.setReglaObtencion(
                campo.getReglaObtencion()
        );

        if (version == null) {
            dto.setIdVersionDocumento(null);
            dto.setValor(null);
            dto.setDisponible(false);
            return dto;
        }

        dto.setIdVersionDocumento(
                version.getId()
        );

        Object valor =
                obtenerValor(
                        version,
                        campo.getReglaObtencion()
                );

        dto.setValor(valor);
        dto.setDisponible(valor != null);

        return dto;
    }

    /**
     * Construye la respuesta cuando no existe el documento académico.
     */
    private InformacionDocumentoCertificadoDTO
    construirInformacionSinDocumento(
            Integer idAsignatura,
            CampoPlantilla campo) {

        InformacionDocumentoCertificadoDTO dto =
                new InformacionDocumentoCertificadoDTO();

        dto.setIdAsignatura(idAsignatura);
        dto.setIdDocumentoAcademico(null);
        dto.setIdVersionDocumento(null);

        dto.setCodigoCampo(
                campo.getCodigoCampo()
        );

        dto.setNombreCampo(
                campo.getNombreCampo()
        );

        dto.setFuente(
                campo.getFuente()
        );

        dto.setMetodoObtencion(
                campo.getMetodoObtencion() != null
                        ? campo.getMetodoObtencion().name()
                        : null
        );

        dto.setReglaObtencion(
                campo.getReglaObtencion()
        );

        dto.setValor(null);
        dto.setDisponible(false);

        return dto;
    }

    /**
     * Obtiene el valor del campo documental a partir de la regla
     * definida en la plantilla.
     *
     * Las reglas se corresponden con propiedades de VersionDocumento.
     */
    private Object obtenerValor(
            VersionDocumento version,
            String regla) {

        if (regla == null || regla.isBlank()) {
            return null;
        }

        String reglaNormalizada =
                regla.trim();

        return switch (reglaNormalizada) {

            case "escuela" ->
                    version.getEscuela();

            case "facultad" ->
                    version.getFacultad();

            case "clasificacionCINE" ->
                    version.getClasificacionCINE();

            case "nucleoBasicoConocimiento" ->
                    version.getNucleoBasicoConocimiento();

            case "ciclo" ->
                    version.getCiclo();

            case "nivelFormacion" ->
                    version.getNivelFormacion();

            case "horasTeoricas" ->
                    version.getHorasTeoricas();

            case "horasPracticas" ->
                    version.getHorasPracticas();

            case "horasLaboratorio" ->
                    version.getHorasLaboratorio();

            case "horasIndependientes" ->
                    version.getHorasIndependientes();

            case "creditos" ->
                    version.getCreditos();

            case "requisitos" ->
                    version.getRequisitos();

            case "justificacion" ->
                    version.getJustificacion();

            case "descripcion" ->
                    version.getDescripcion();

            case "proposito" ->
                    version.getProposito();

            case "modoCalificacion" ->
                    version.getModoCalificacion();

            case "modalidades" ->
                    version.getModalidades();

            case "actaAprobacion" ->
                    version.getActaAprobacion();

            case "actaModificacion" ->
                    version.getActaModificacion();

            case "observaciones" ->
                    version.getObservaciones();

            case "idProgramaDisena" ->
                    version.getIdProgramaDisena();

            case "periodo" ->
                    version.getPeriodo();

            case "nombreArchivo" ->
                    version.getNombreArchivo();

            case "fechaCarga" ->
                    version.getFechaCarga();

            default ->
                    null;
        };
    }

    private DocumentoCertificadoDTO construirDTO(
            Integer idAsignatura,
            TipoDocumentoAcademico tipo,
            DocumentoAcademico documento) {

        DocumentoCertificadoDTO dto =
                new DocumentoCertificadoDTO();

        dto.setIdAsignatura(idAsignatura);

        dto.setIdTipoDocumentoAcademico(
                tipo.getId()
        );

        dto.setTipoDocumento(
                tipo.getCodigo()
        );

        if (documento == null) {

            dto.setIdDocumentoAcademico(null);
            dto.setCodigoFormato(null);
            dto.setVersionFormato(null);
            dto.setVersionesDisponibles(0);
            dto.setDisponible(false);

            return dto;
        }

        dto.setIdDocumentoAcademico(
                documento.getId()
        );

        dto.setCodigoFormato(
                documento.getCodigoFormato()
        );

        dto.setVersionFormato(
                documento.getVersionFormato()
        );

        int cantidadVersiones =
                versionRepository
                        .findByIdDocumentoAcademico(
                                documento.getId()
                        )
                        .size();

        dto.setVersionesDisponibles(
                cantidadVersiones
        );

        dto.setDisponible(
                cantidadVersiones > 0
        );

        return dto;
    }
}