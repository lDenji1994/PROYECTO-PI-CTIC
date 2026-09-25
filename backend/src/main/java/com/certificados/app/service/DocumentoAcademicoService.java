package com.certificados.app.service;

import com.certificados.app.dto.DocumentoResumenDTO;
import com.certificados.app.dto.VersionResumenDTO;
import com.certificados.app.exception.BusinessException;
import com.certificados.app.exception.ResourceNotFoundException;
import com.certificados.app.model.Asignatura;
import com.certificados.app.model.DocumentoAcademico;
import com.certificados.app.model.TipoDocumentoAcademico;
import com.certificados.app.model.VersionDocumento;
import com.certificados.app.repository.AsignaturaRepository;
import com.certificados.app.repository.DocumentoAcademicoRepository;
import com.certificados.app.repository.ProgramaRepository;
import com.certificados.app.repository.TipoDocumentoAcademicoRepository;
import com.certificados.app.repository.VersionDocumentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Documentos academicos (una carta descriptiva / syllabus por asignatura
 * y tipo de documento) y sus versiones cargadas.
 *
 * Flujo del panel "Carga de informacion" -> {@link #cargarDocumento}:
 *   1. Busca el documento de esa asignatura + tipo (o lo crea).
 *   2. Si el codigo/version de formato cambio, lo actualiza.
 *   3. Guarda el PDF como una VERSION nueva (VersionDocumentoService).
 *   Todo en una sola transaccion: si algo falla, no queda nada a medias.
 *
 * SE PUEDE MODIFICAR: mensajes, validaciones adicionales.
 * NO MODIFICAR: la unicidad asignatura + tipo (la exige la BD).
 */
@Service
@Transactional
public class DocumentoAcademicoService {

    private final DocumentoAcademicoRepository repository;
    private final AsignaturaRepository asignaturaRepository;
    private final TipoDocumentoAcademicoRepository tipoRepository;
    private final VersionDocumentoRepository versionRepository;
    private final ProgramaRepository programaRepository;
    private final VersionDocumentoService versionDocumentoService;
    private final FormatoService formatoService;
    private final ActividadService actividadService;

    public DocumentoAcademicoService(
            DocumentoAcademicoRepository repository,
            AsignaturaRepository asignaturaRepository,
            TipoDocumentoAcademicoRepository tipoRepository,
            VersionDocumentoRepository versionRepository,
            ProgramaRepository programaRepository,
            VersionDocumentoService versionDocumentoService,
            FormatoService formatoService,
            ActividadService actividadService) {
        this.repository = repository;
        this.asignaturaRepository = asignaturaRepository;
        this.tipoRepository = tipoRepository;
        this.versionRepository = versionRepository;
        this.programaRepository = programaRepository;
        this.versionDocumentoService = versionDocumentoService;
        this.formatoService = formatoService;
        this.actividadService = actividadService;
    }

    /* ======================= Consultas basicas ======================= */

    @Transactional(readOnly = true)
    public List<DocumentoAcademico> listarTodos() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public DocumentoAcademico buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Documento académico no encontrado con id " + id));
    }

    @Transactional(readOnly = true)
    public List<DocumentoAcademico> listarPorAsignatura(Integer idAsignatura) {
        return repository.findByIdAsignatura(idAsignatura);
    }

    /** POST /api/documentos-academicos (JSON, sin archivo). */
    public DocumentoAcademico guardar(DocumentoAcademico documento) {
        Asignatura asignatura = obtenerAsignatura(documento.getIdAsignatura());
        obtenerTipo(documento.getIdTipoDocumentoAcademico());
        validarFormato(documento.getCodigoFormato(), documento.getVersionFormato());

        if (documento.getId() == null && repository.findByIdAsignaturaAndIdTipoDocumentoAcademico(
                documento.getIdAsignatura(), documento.getIdTipoDocumentoAcademico()).isPresent()) {
            throw new BusinessException("La asignatura ya tiene un documento de ese tipo");
        }

        documento.setCodigoFormato(FormatoService.normalizarCodigo(documento.getCodigoFormato()));
        documento.setVersionFormato(documento.getVersionFormato().trim());
        DocumentoAcademico guardado = repository.save(documento);

        actividadService.registrar(ActividadService.TABLA_DOCUMENTOS, ActividadService.CREAR_DOCUMENTO,
                asignatura.getCodigo() + " - " + asignatura.getNombre());
        return guardado;
    }

    /* ======================= Carga completa (panel) ======================= */

    public DocumentoResumenDTO cargarDocumento(
            Integer idAsignatura,
            Integer idTipoDocumento,
            String codigoFormato,
            String versionFormato,
            String periodo,
            Integer idPrograma,
            MultipartFile archivo) throws IOException {

        Asignatura asignatura = obtenerAsignatura(idAsignatura);
        TipoDocumentoAcademico tipo = obtenerTipo(idTipoDocumento);
        validarFormato(codigoFormato, versionFormato);

        if (idPrograma != null && !programaRepository.existsById(idPrograma)) {
            throw new ResourceNotFoundException("Programa no encontrado con id " + idPrograma);
        }

        String codigo = FormatoService.normalizarCodigo(codigoFormato);
        String version = versionFormato.trim();
        String etiqueta = asignatura.getCodigo() + " - " + asignatura.getNombre() + " / " + tipo.getNombre();

        DocumentoAcademico documento = repository
                .findByIdAsignaturaAndIdTipoDocumentoAcademico(idAsignatura, idTipoDocumento)
                .orElse(null);

        if (documento == null) {
            documento = new DocumentoAcademico();
            documento.setIdAsignatura(idAsignatura);
            documento.setIdTipoDocumentoAcademico(idTipoDocumento);
            documento.setCodigoFormato(codigo);
            documento.setVersionFormato(version);
            documento = repository.save(documento);
            actividadService.registrar(ActividadService.TABLA_DOCUMENTOS,
                    ActividadService.CREAR_DOCUMENTO, etiqueta);
        } else if (!codigo.equals(FormatoService.normalizarCodigo(documento.getCodigoFormato()))
                || !FormatoService.normalizarVersion(version)
                        .equals(FormatoService.normalizarVersion(documento.getVersionFormato()))) {
            String anterior = documento.getCodigoFormato() + " v" + documento.getVersionFormato();
            documento.setCodigoFormato(codigo);
            documento.setVersionFormato(version);
            documento = repository.save(documento);
            actividadService.registrar(ActividadService.TABLA_DOCUMENTOS, ActividadService.ACTUALIZAR_FORMATO,
                    etiqueta + ": " + anterior + " -> " + codigo + " v" + version);
        }

        // Guarda el PDF como nueva version (valida PDF, tamano y deja su propia bitacora)
        VersionDocumento nueva = versionDocumentoService.cargarArchivo(archivo, documento.getId(), periodo);
        if (idPrograma != null) {
            nueva.setIdProgramaDisena(idPrograma);
            versionRepository.save(nueva);
        }

        return resumen(documento.getId());
    }

    /* ======================= Resumen para el panel ======================= */

    /** Documento con asignatura, tipo, vigencia de formato y versiones (sin binarios). */
    @Transactional(readOnly = true)
    public DocumentoResumenDTO resumen(Integer idDocumento) {
        DocumentoAcademico doc = buscarPorId(idDocumento);
        return listarResumen(doc.getIdAsignatura()).stream()
                .filter(d -> d.id().equals(idDocumento))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado"));
    }

    /**
     * Lista de documentos ordenada por la ultima carga (mas reciente primero).
     * @param idAsignatura opcional: solo los de esa asignatura
     */
    @Transactional(readOnly = true)
    public List<DocumentoResumenDTO> listarResumen(Integer idAsignatura) {
        List<DocumentoAcademico> documentos = idAsignatura == null
                ? repository.findAll()
                : repository.findByIdAsignatura(idAsignatura);

        Map<Integer, Asignatura> asignaturas = asignaturaRepository.findAll().stream()
                .collect(Collectors.toMap(Asignatura::getId, Function.identity()));
        Map<Integer, TipoDocumentoAcademico> tipos = tipoRepository.findAll().stream()
                .collect(Collectors.toMap(TipoDocumentoAcademico::getId, Function.identity()));
        Map<Integer, List<VersionResumenDTO>> versiones = versionRepository.listarResumen().stream()
                .collect(Collectors.groupingBy(VersionResumenDTO::idDocumentoAcademico,
                        LinkedHashMap::new, Collectors.toList()));

        return documentos.stream()
                .map(d -> aResumen(d, asignaturas.get(d.getIdAsignatura()), tipos.get(d.getIdTipoDocumentoAcademico()),
                        versiones.getOrDefault(d.getId(), List.of())))
                .sorted(Comparator.comparing(
                        (DocumentoResumenDTO r) -> r.ultimaVersion() == null ? LocalDateTime.MIN : r.ultimaVersion().fechaCarga(),
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }

    private DocumentoResumenDTO aResumen(DocumentoAcademico d, Asignatura a, TipoDocumentoAcademico t,
                                         List<VersionResumenDTO> versiones) {
        String[] codigos = a == null ? new String[]{"", ""} : AsignaturaService.separar(a.getCodigo());
        boolean vigente = formatoService.esVigente(d.getCodigoFormato(), d.getVersionFormato());
        String nombreFormato = formatoService.nombreDe(d.getCodigoFormato());
        return new DocumentoResumenDTO(
                d.getId(),
                d.getIdAsignatura(),
                codigos[0],
                codigos[1],
                a == null ? null : a.getCodigo(),
                a == null ? "(asignatura eliminada)" : a.getNombre(),
                d.getIdTipoDocumentoAcademico(),
                t == null ? null : t.getNombre(),
                d.getCodigoFormato(),
                d.getVersionFormato(),
                nombreFormato != null ? nombreFormato : "Formato no catalogado",
                vigente,
                vigente ? "VIGENTE" : "DESACTUALIZADO",
                versiones.size(),
                versiones.isEmpty() ? null : versiones.get(0),
                versiones);
    }

    /* ======================= Validaciones ======================= */

    private Asignatura obtenerAsignatura(Integer id) {
        if (id == null) {
            throw new BusinessException("La asignatura es obligatoria");
        }
        return asignaturaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignatura no encontrada con id " + id));
    }

    private TipoDocumentoAcademico obtenerTipo(Integer id) {
        if (id == null) {
            throw new BusinessException("El tipo de documento es obligatorio");
        }
        return tipoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de documento no encontrado con id " + id));
    }

    private void validarFormato(String codigoFormato, String versionFormato) {
        if (codigoFormato == null || codigoFormato.isBlank()) {
            throw new BusinessException("El código de formato es obligatorio (ej. DA-FO-085N)");
        }
        if (versionFormato == null || versionFormato.isBlank()) {
            throw new BusinessException("La versión del formato es obligatoria (ej. 03)");
        }
        if (!FormatoService.normalizarCodigo(codigoFormato).matches("^[A-Z0-9\\-_.]{2,50}$")) {
            throw new BusinessException("Código de formato inválido: solo letras, números y guiones");
        }
        if (!versionFormato.trim().matches("^[A-Za-z0-9.\\-]{1,20}$")) {
            throw new BusinessException("Versión de formato inválida (ej. 03)");
        }
    }
}
