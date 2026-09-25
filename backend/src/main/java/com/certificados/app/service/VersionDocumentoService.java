package com.certificados.app.service;

import com.certificados.app.exception.ResourceNotFoundException;
import com.certificados.app.dto.VersionDocumentoDTO;
import com.certificados.app.model.DocumentoAcademico;
import com.certificados.app.model.VersionDocumento;
import com.certificados.app.repository.AsignaturaRepository;
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
    private final AsignaturaRepository asignaturaRepository;
    private final FormatoService formatoService;
    private final ActividadService actividadService;

    public VersionDocumentoService(
            VersionDocumentoRepository repository,
            DocumentoAcademicoRepository documentoAcademicoRepository,
            AsignaturaRepository asignaturaRepository,
            FormatoService formatoService,
            ActividadService actividadService) {

        this.repository = repository;
        this.documentoAcademicoRepository = documentoAcademicoRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.formatoService = formatoService;
        this.actividadService = actividadService;
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

        String nombreArchivo = limpiarNombreArchivo(archivo.getOriginalFilename());

        if (nombreArchivo.isBlank()) {
            throw new IllegalArgumentException(
                    "El archivo debe tener un nombre válido"
            );
        }

        byte[] contenido = archivo.getBytes();

        // Seguridad: el tipo MIME lo manda el navegador y se puede falsificar,
        // asi que tambien se verifica la firma real de un PDF ("%PDF").
        if (contenido.length < 4
                || contenido[0] != '%' || contenido[1] != 'P'
                || contenido[2] != 'D' || contenido[3] != 'F') {
            throw new IllegalArgumentException(
                    "El archivo no es un PDF válido"
            );
        }

        String periodoLimpio = (periodo == null || periodo.isBlank()) ? null : periodo.trim();
        if (periodoLimpio != null && !periodoLimpio.matches("^\\d{4}-\\d{1,2}$")) {
            throw new IllegalArgumentException(
                    "El periodo debe tener el formato AAAA-N (ej. 2026-2)"
            );
        }

        VersionDocumento version = new VersionDocumento();

        version.setIdDocumentoAcademico(idDocumentoAcademico);
        version.setPeriodo(periodoLimpio);
        version.setNombreArchivo(nombreArchivo);
        version.setArchivo(contenido);

        VersionDocumento guardada = repository.save(version);

        registrarCarga(idDocumentoAcademico, guardada);

        return guardada;
    }

    /**
     * Bitacora: deja constancia de la carga y avisa si el formato del
     * documento esta desactualizado (se ve en amarillo en el Dashboard).
     */
    private void registrarCarga(Integer idDocumentoAcademico, VersionDocumento version) {
        DocumentoAcademico documento = documentoAcademicoRepository
                .findById(idDocumentoAcademico).orElse(null);
        if (documento == null) {
            return;
        }
        String asignatura = asignaturaRepository.findById(documento.getIdAsignatura())
                .map(a -> a.getCodigo() + " - " + a.getNombre())
                .orElse("Asignatura #" + documento.getIdAsignatura());
        boolean vigente = formatoService.esVigente(
                documento.getCodigoFormato(), documento.getVersionFormato());

        actividadService.registrar(
                ActividadService.TABLA_VERSIONES,
                vigente ? ActividadService.CARGAR_DOCUMENTO : ActividadService.CARGAR_DOCUMENTO_ANTIGUO,
                asignatura + " (" + documento.getCodigoFormato() + " v" + documento.getVersionFormato()
                        + (version.getPeriodo() != null ? ", " + version.getPeriodo() : "") + ")");
    }

    /**
     * Deja solo un nombre de archivo seguro: sin rutas (../), sin comillas
     * ni saltos de linea (evita inyeccion en la cabecera de descarga).
     */
    static String limpiarNombreArchivo(String original) {
        if (original == null) {
            return "";
        }
        String nombre = original.replace('\\', '/');
        nombre = nombre.substring(nombre.lastIndexOf('/') + 1);
        nombre = nombre.replaceAll("[^\\p{L}\\p{N} ._()\\-]", "_").trim();
        if (nombre.length() > 200) {
            nombre = nombre.substring(nombre.length() - 200);
        }
        return nombre;
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
                        new ResourceNotFoundException(
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
