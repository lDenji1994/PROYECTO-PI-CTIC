package com.certificados.app.service;

import com.certificados.app.dto.DatosCursoDTO;
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
 * Documentos academicos (carta descriptiva, syllabus...) de cada asignatura
 * y el archivo cargado de cada uno.
 *
 * Desde la migracion 05 una asignatura puede tener todos los documentos que
 * se necesiten, incluso varios del mismo tipo (p. ej. la carta descriptiva de
 * 2019 y la de 2026). Cada archivo que se monta queda como SU PROPIO
 * documento, con su tipo, su formato y su periodo.
 *
 * Flujo del panel "Carga de informacion" -> {@link #cargarDocumento}:
 *   1. Crea el documento (asignatura + tipo + formato).
 *   2. Guarda el archivo y los datos del curso (VersionDocumentoService).
 *   Todo en una sola transaccion: si algo falla, no queda nada a medias.
 *
 * Corregir una carga -> {@link #corregirCarga}: cambia asignatura, tipo,
 * formato, periodo o programa de un archivo ya cargado.
 *
 * SE PUEDE MODIFICAR: mensajes, validaciones adicionales.
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
            DatosCursoDTO datos,
            MultipartFile archivo) throws IOException {

        if (datos == null) {
            throw new BusinessException("Faltan los datos del curso (descripción, contenido, créditos y horas)");
        }

        Asignatura asignatura = obtenerAsignatura(idAsignatura);
        TipoDocumentoAcademico tipo = obtenerTipo(idTipoDocumento);
        validarFormato(codigoFormato, versionFormato);

        if (idPrograma != null && !programaRepository.existsById(idPrograma)) {
            throw new ResourceNotFoundException("Programa no encontrado con id " + idPrograma);
        }

        String codigo = FormatoService.normalizarCodigo(codigoFormato);
        String version = versionFormato.trim();
        String etiqueta = asignatura.getCodigo() + " - " + asignatura.getNombre() + " / " + tipo.getNombre();

        // Cada archivo cargado es su propio documento (con su tipo y formato)
        DocumentoAcademico documento = new DocumentoAcademico();
        documento.setIdAsignatura(idAsignatura);
        documento.setIdTipoDocumentoAcademico(idTipoDocumento);
        documento.setCodigoFormato(codigo);
        documento.setVersionFormato(version);
        documento = repository.save(documento);
        actividadService.registrar(ActividadService.TABLA_DOCUMENTOS,
                ActividadService.CREAR_DOCUMENTO, etiqueta);

        // Guarda el archivo (PDF, Word o Excel) como nueva version junto con los
        // datos del curso escritos a mano. Si algo falla (archivo danado, dato
        // invalido...) se revierte TODO: no queda un documento a medias.
        VersionDocumento nueva = versionDocumentoService.cargarArchivo(
                archivo, documento.getId(), periodo, datos);
        if (idPrograma != null) {
            nueva.setIdProgramaDisena(idPrograma);
            versionRepository.save(nueva);
        }

        return resumen(documento.getId());
    }

    /* ======================= Corregir una carga ======================= */

    /**
     * Corrige los datos de identificacion de un archivo ya cargado: la
     * asignatura, el tipo de documento (p. ej. era un Syllabus y no una
     * Carta descriptiva), el formato, el periodo y el programa que disena
     * el curso. Los datos del curso y el archivo se corrigen aparte.
     *
     * Si el documento comparte registro con otros archivos (cargas hechas
     * antes de la migracion 05), este archivo se separa en un documento
     * propio para no cambiar los demas.
     */
    public DocumentoResumenDTO corregirCarga(Integer idVersion, Integer idAsignatura, Integer idTipoDocumento,
                                             String codigoFormato, String versionFormato,
                                             String periodo, Integer idPrograma) {
        VersionDocumento cargada = versionRepository.findById(idVersion)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El archivo ya no existe (id " + idVersion + "). Actualiza la página."));
        DocumentoAcademico actual = buscarPorId(cargada.getIdDocumentoAcademico());
        if (idAsignatura == null || idTipoDocumento == null) {
            throw new BusinessException("La asignatura y el tipo de documento son obligatorios");
        }

        Asignatura asignatura = obtenerAsignatura(idAsignatura);
        TipoDocumentoAcademico tipo = obtenerTipo(idTipoDocumento);
        validarFormato(codigoFormato, versionFormato);
        String codigo = FormatoService.normalizarCodigo(codigoFormato);
        String version = versionFormato.trim();
        String periodoLimpio = (periodo == null || periodo.isBlank()) ? null : periodo.trim();
        if (periodoLimpio != null && !periodoLimpio.matches("^\\d{4}-\\d{1,2}$")) {
            throw new BusinessException("El periodo debe tener el formato AAAA-N (ej. 2026-2)");
        }
        if (idPrograma != null && !programaRepository.existsById(idPrograma)) {
            throw new ResourceNotFoundException("Programa no encontrado con id " + idPrograma);
        }

        String antes = descripcionCarga(actual, cargada.getPeriodo());

        boolean cambiaDocumento = !idAsignatura.equals(actual.getIdAsignatura())
                || !idTipoDocumento.equals(actual.getIdTipoDocumentoAcademico())
                || !codigo.equals(FormatoService.normalizarCodigo(actual.getCodigoFormato()))
                || !FormatoService.normalizarVersion(version)
                        .equals(FormatoService.normalizarVersion(actual.getVersionFormato()));
        DocumentoAcademico destino = actual;
        if (cambiaDocumento) {
            if (versionRepository.countByIdDocumentoAcademico(actual.getId()) > 1) {
                // Lo comparte con otros archivos: este se separa en un documento nuevo
                destino = new DocumentoAcademico();
            }
            destino.setIdAsignatura(idAsignatura);
            destino.setIdTipoDocumentoAcademico(idTipoDocumento);
            destino.setCodigoFormato(codigo);
            destino.setVersionFormato(version);
            destino = repository.save(destino);
        }

        cargada.setIdDocumentoAcademico(destino.getId());
        cargada.setPeriodo(periodoLimpio);
        cargada.setIdProgramaDisena(idPrograma);
        versionRepository.save(cargada);

        actividadService.registrar(ActividadService.TABLA_VERSIONES, ActividadService.CORREGIR_CARGA,
                cargada.getNombreArchivo() + ": " + antes + " -> " + asignatura.getCodigo() + " / "
                        + tipo.getNombre() + " " + codigo + " v" + version
                        + (periodoLimpio == null ? "" : " (periodo " + periodoLimpio + ")"));
        return resumen(destino.getId());
    }

    private String descripcionCarga(DocumentoAcademico d, String periodo) {
        String asignatura = asignaturaRepository.findById(d.getIdAsignatura())
                .map(Asignatura::getCodigo).orElse("#" + d.getIdAsignatura());
        String tipo = tipoRepository.findById(d.getIdTipoDocumentoAcademico())
                .map(TipoDocumentoAcademico::getNombre).orElse("");
        return asignatura + " / " + tipo + " " + d.getCodigoFormato() + " v" + d.getVersionFormato()
                + (periodo == null ? "" : " (periodo " + periodo + ")");
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
