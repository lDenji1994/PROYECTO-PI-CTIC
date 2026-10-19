package com.certificados.app.service;

import com.certificados.app.dto.AsignaturaCertificadoDTO;
import com.certificados.app.exception.ResourceNotFoundException;
import com.certificados.app.model.Programa;
import com.certificados.app.model.SolicitudCertificado;
import com.certificados.app.model.TipoContenidoCertificado;
import com.certificados.app.model.VersionDocumento;
import com.certificados.app.repository.ProgramaRepository;
import com.certificados.app.repository.SolicitudCertificadoRepository;
import com.certificados.app.repository.VersionDocumentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * TEXTOS DEL MEMBRETE DE UN CERTIFICADO (los usa el PDF y la vista previa).
 *
 * La escuela y la facultad salen del programa de la solicitud:
 *   - Solicitud por semestres / programa completo: el programa elegido.
 *   - Solicitud por asignaturas: el programa que diseña la mayoria de los
 *     documentos usados (campo "Programa que diseña el curso" de la carga).
 * Si el programa no tiene escuela o facultad registradas, se usa el valor
 * general (MembreteCertificadoPdf / application.properties).
 */
@Service
@Transactional(readOnly = true)
public class MembreteService {

    /** Lo que se escribe en el membrete de cada pagina. */
    public record Textos(String escuela, String facultad, String titulo, String estudiante, String piePagina) {
    }

    private final SolicitudCertificadoRepository solicitudRepository;
    private final ProgramaRepository programaRepository;
    private final VersionDocumentoRepository versionRepository;
    private final ContenidoCertificadoService contenidoCertificadoService;
    private final MembreteCertificadoPdf membrete;

    public MembreteService(SolicitudCertificadoRepository solicitudRepository,
                           ProgramaRepository programaRepository,
                           VersionDocumentoRepository versionRepository,
                           ContenidoCertificadoService contenidoCertificadoService,
                           MembreteCertificadoPdf membrete) {
        this.solicitudRepository = solicitudRepository;
        this.programaRepository = programaRepository;
        this.versionRepository = versionRepository;
        this.contenidoCertificadoService = contenidoCertificadoService;
        this.membrete = membrete;
    }

    /** Para la vista previa del panel. */
    public Textos textos(Integer idSolicitud) {
        SolicitudCertificado solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con id " + idSolicitud));
        return textos(solicitud, contenidoCertificadoService.obtener(idSolicitud));
    }

    /** Para el PDF (ya tiene las asignaturas calculadas). */
    public Textos textos(SolicitudCertificado solicitud, List<AsignaturaCertificadoDTO> asignaturas) {
        Programa programa = programaDelCertificado(solicitud, asignaturas);
        String escuela = programa != null && programa.getEscuela() != null ? programa.getEscuela() : membrete.escuela();
        String facultad = programa != null && programa.getFacultad() != null ? programa.getFacultad() : membrete.facultad();
        String titulo = solicitud.getTipoContenido() == TipoContenidoCertificado.COMPLETO
                ? "CONTENIDOS PROGRAMÁTICOS"
                : "RESUMEN CONTENIDOS PROGRAMÁTICOS";
        String id = "ID. " + String.format("%09d", solicitud.getIdEstudiante());
        String nombre = solicitud.getNombreEstudiante();
        String estudiante = nombre == null || nombre.isBlank() ? id : nombre.trim() + ", " + id;
        return new Textos(escuela, facultad, titulo, estudiante, membrete.piePagina());
    }

    private Programa programaDelCertificado(SolicitudCertificado solicitud, List<AsignaturaCertificadoDTO> asignaturas) {
        if (solicitud.getIdPrograma() != null) {
            return programaRepository.findById(solicitud.getIdPrograma()).orElse(null);
        }
        List<Integer> versiones = asignaturas.stream()
                .map(AsignaturaCertificadoDTO::idVersionDocumento)
                .filter(Objects::nonNull)
                .toList();
        if (versiones.isEmpty()) {
            return null;
        }
        Map<Integer, Long> porPrograma = versionRepository.findAllById(versiones).stream()
                .map(VersionDocumento::getIdProgramaDisena)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return porPrograma.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .flatMap(e -> programaRepository.findById(e.getKey()))
                .orElse(null);
    }
}
