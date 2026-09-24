package com.certificados.app.service;

import com.certificados.app.dto.DocumentoCertificadoDTO;
import com.certificados.app.model.DetalleSolicitudCertificado;
import com.certificados.app.model.DocumentoAcademico;
import com.certificados.app.model.TipoDocumentoAcademico;
import com.certificados.app.repository.DetalleSolicitudCertificadoRepository;
import com.certificados.app.repository.DocumentoAcademicoRepository;
import com.certificados.app.repository.TipoDocumentoAcademicoRepository;
import com.certificados.app.repository.VersionDocumentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DocumentoCertificadoService {

    private final DetalleSolicitudCertificadoRepository detalleRepository;
    private final DocumentoAcademicoRepository documentoRepository;
    private final TipoDocumentoAcademicoRepository tipoDocumentoRepository;
    private final VersionDocumentoRepository versionRepository;

    public DocumentoCertificadoService(
            DetalleSolicitudCertificadoRepository detalleRepository,
            DocumentoAcademicoRepository documentoRepository,
            TipoDocumentoAcademicoRepository tipoDocumentoRepository,
            VersionDocumentoRepository versionRepository) {

        this.detalleRepository = detalleRepository;
        this.documentoRepository = documentoRepository;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.versionRepository = versionRepository;
    }

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
                    documentoRepository.findByIdAsignatura(
                            idAsignatura
                    );

            for (TipoDocumentoAcademico tipo : tipos) {

                DocumentoAcademico documento =
                        documentos.stream()
                                .filter(d ->
                                        d.getIdTipoDocumentoAcademico()
                                                .equals(tipo.getId())
                                )
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