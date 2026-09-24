package com.certificados.app.service;

import com.certificados.app.dto.VersionDocumentoDTO;
import com.certificados.app.model.VersionDocumento;
import com.certificados.app.repository.DocumentoAcademicoRepository;
import com.certificados.app.repository.VersionDocumentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
public class VersionDocumentoService {

    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;

    private final VersionDocumentoRepository repository;
    private final DocumentoAcademicoRepository documentoAcademicoRepository;

    public VersionDocumentoService(
            VersionDocumentoRepository repository,
            DocumentoAcademicoRepository documentoAcademicoRepository) {

        this.repository = repository;
        this.documentoAcademicoRepository = documentoAcademicoRepository;
    }

    public List<VersionDocumentoDTO> listarTodos() {
        return repository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public VersionDocumentoDTO buscarPorId(Integer id) {
        return convertirADTO(obtenerEntidad(id));
    }

    public List<VersionDocumentoDTO> listarPorDocumento(
            Integer idDocumentoAcademico) {

        verificarDocumentoAcademico(idDocumentoAcademico);

        return repository.findByIdDocumentoAcademico(idDocumentoAcademico)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public VersionDocumento cargarArchivo(
            MultipartFile archivo,
            Integer idDocumentoAcademico,
            String periodo) throws IOException {

        verificarDocumentoAcademico(idDocumentoAcademico);

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "El archivo no puede estar vacío"
            );
        }

        if (archivo.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "El archivo no puede superar los 20 MB"
            );
        }

        if (!"application/pdf".equalsIgnoreCase(
                archivo.getContentType())) {

            throw new IllegalArgumentException(
                    "Solo se permiten archivos PDF"
            );
        }

        String nombreArchivo = archivo.getOriginalFilename();

        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            throw new IllegalArgumentException(
                    "El archivo debe tener un nombre válido"
            );
        }

        VersionDocumento version = new VersionDocumento();

        version.setIdDocumentoAcademico(idDocumentoAcademico);
        version.setPeriodo(periodo);
        version.setNombreArchivo(nombreArchivo);
        version.setArchivo(archivo.getBytes());

        return repository.save(version);
    }

    public byte[] obtenerArchivo(Integer id) {
        return obtenerEntidad(id).getArchivo();
    }

    public String obtenerNombreArchivo(Integer id) {
        return obtenerEntidad(id).getNombreArchivo();
    }

    private VersionDocumento obtenerEntidad(Integer id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Versión de documento no encontrada con id " + id
                        )
                );
    }

    private void verificarDocumentoAcademico(
            Integer idDocumentoAcademico) {

        if (idDocumentoAcademico == null) {
            throw new IllegalArgumentException(
                    "El documento académico es obligatorio"
            );
        }

        if (!documentoAcademicoRepository.existsById(
                idDocumentoAcademico)) {

            throw new IllegalArgumentException(
                    "No existe el documento académico con id "
                            + idDocumentoAcademico
            );
        }
    }

    private VersionDocumentoDTO convertirADTO(
            VersionDocumento version) {

        VersionDocumentoDTO dto = new VersionDocumentoDTO();

        dto.setId(version.getId());
        dto.setIdDocumentoAcademico(
                version.getIdDocumentoAcademico()
        );
        dto.setPeriodo(version.getPeriodo());
        dto.setNombreArchivo(version.getNombreArchivo());
        dto.setEscuela(version.getEscuela());
        dto.setFacultad(version.getFacultad());
        dto.setClasificacionCINE(
                version.getClasificacionCINE()
        );
        dto.setNucleoBasicoConocimiento(
                version.getNucleoBasicoConocimiento()
        );
        dto.setCiclo(version.getCiclo());
        dto.setNivelFormacion(version.getNivelFormacion());
        dto.setHorasTeoricas(version.getHorasTeoricas());
        dto.setHorasPracticas(version.getHorasPracticas());
        dto.setHorasLaboratorio(
                version.getHorasLaboratorio()
        );
        dto.setHorasIndependientes(
                version.getHorasIndependientes()
        );
        dto.setCreditos(version.getCreditos());
        dto.setRequisitos(version.getRequisitos());
        dto.setJustificacion(version.getJustificacion());
        dto.setDescripcion(version.getDescripcion());
        dto.setProposito(version.getProposito());
        dto.setModoCalificacion(
                version.getModoCalificacion()
        );
        dto.setModalidades(version.getModalidades());
        dto.setActaAprobacion(
                version.getActaAprobacion()
        );
        dto.setActaModificacion(
                version.getActaModificacion()
        );
        dto.setObservaciones(version.getObservaciones());
        dto.setIdProgramaDisena(
                version.getIdProgramaDisena()
        );
        dto.setFechaCarga(version.getFechaCarga());

        return dto;
    }
}      
